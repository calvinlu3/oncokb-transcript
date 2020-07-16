package org.mskcc.cbio.web.rest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.genome_nexus.ApiException;
import org.genome_nexus.client.EnsemblControllerApi;
import org.genome_nexus.client.EnsemblTranscript;
import org.mskcc.cbio.config.ApplicationProperties;
import org.mskcc.cbio.service.UrlService;
import org.mskcc.cbio.web.rest.errors.BadRequestException;
import org.mskcc.cbio.web.rest.vm.*;
import org.mskcc.cbio.web.rest.vm.ensembl.Sequence;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.*;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller to authenticate users.
 */
@RestController
@RequestMapping("/api")
public class TranscriptController {
    private final UrlService urlService;

    public TranscriptController(ApplicationProperties applicationProperties, UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/compare-transcript/{hugoSymbol}")
    public ResponseEntity<TranscriptComparisonResultVM> authorize(
        @PathVariable String hugoSymbol,
        @RequestBody TranscriptComparisonVM transcriptComparisonVM
    ) throws ApiException {
        TranscriptComparisonResultVM result = new TranscriptComparisonResultVM();

        // Find whether both transcript length are the same
        Optional<EnsemblTranscript> ensemblA = getEnsemblTranscript(hugoSymbol, transcriptComparisonVM.getTranscriptA());
        Optional<EnsemblTranscript> ensemblB = getEnsemblTranscript(hugoSymbol, transcriptComparisonVM.getTranscriptB());

        Optional<Sequence> sequenceA = Optional.empty();
        if (ensemblA.isPresent()) {
            sequenceA = getProteinSequence(transcriptComparisonVM.getTranscriptA().getReferenceGenome(), ensemblA.get().getProteinId());
            result.setSequenceA(sequenceA.orElse(new Sequence()).getSeq());
        }

        Optional<Sequence> sequenceB = Optional.empty();
        if (ensemblB.isPresent()) {
            sequenceB = getProteinSequence(transcriptComparisonVM.getTranscriptB().getReferenceGenome(), ensemblB.get().getProteinId());
            result.setSequenceB(sequenceB.orElse(new Sequence()).getSeq());
        }

        if (ensemblA.get().getProteinLength().equals(ensemblB.get().getProteinLength())) {
            // do a quick check whether the protein is the same
            if (sequenceA.isPresent() && sequenceB.isPresent()) {
                if (sequenceA.get().getSeq().equals(sequenceB.get().getSeq())) {
                    result.setMatch(true);
                }
            }
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/match-transcript/{hugoSymbol}")
    public ResponseEntity<TranscriptMatchResultVM> authorize(
        @PathVariable String hugoSymbol,
        @RequestBody MatchTranscriptVM matchTranscriptVM
    ) throws ApiException {
        // Find whether both transcript length are the same
        Optional<EnsemblTranscript> _ensemblTranscript = getEnsemblTranscript(hugoSymbol, matchTranscriptVM.getTranscript());

        if (_ensemblTranscript.isPresent()) {
            Optional<Sequence> _sequence = getProteinSequence(matchTranscriptVM.getTranscript().getReferenceGenome(), _ensemblTranscript.get().getProteinId());
            if (_sequence.isPresent()) {
                List<EnsemblTranscript> targetEnsemblTranscripts = getEnsemblTranscriptList(hugoSymbol, matchTranscriptVM.getTargetReferenceGenome());
                if (targetEnsemblTranscripts.size() == 0) {
                    throw new BadRequestException("The target reference genome does not have any ensembl transcripts.");
                } else {
                    return new ResponseEntity<>(pickEnsemblTranscript(matchTranscriptVM.getTargetReferenceGenome(), targetEnsemblTranscripts, _sequence.get()), HttpStatus.OK);
                }
            } else {
                throw new BadRequestException("The transcript is invalid");
            }
        } else {
            throw new BadRequestException("The transcript is invalid");
        }
    }

    private List<EnsemblTranscript> getEnsemblTranscriptList(String hugoSymbol, REFERENCE_GENOME referenceGenome) throws ApiException {
        EnsemblControllerApi controllerApi = urlService.getEnsemblControllerApi(referenceGenome);
        return controllerApi.fetchEnsemblTranscriptsGET(null, null, hugoSymbol);
    }

    private Optional<EnsemblTranscript> getEnsemblTranscript(String hugoSymbol, TranscriptPairVM transcriptPairVM) throws ApiException {
        return getEnsemblTranscriptList(hugoSymbol, transcriptPairVM.getReferenceGenome()).stream().filter(ensemblTranscript -> !StringUtils.isEmpty(ensemblTranscript.getTranscriptId()) && ensemblTranscript.getTranscriptId().equalsIgnoreCase(transcriptPairVM.getTranscript())).findFirst();
    }

    private Optional<Sequence> getProteinSequence(REFERENCE_GENOME referenceGenome, String transcript) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);

        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<Sequence> response = restTemplate.exchange(
                urlService.getEnsemblSequenceGETUrl(referenceGenome, transcript), HttpMethod.GET, entity, Sequence.class);
            return Optional.of(response.getBody());
        } catch (HttpClientErrorException exception) {
            throw new BadRequestException("reference genome " + referenceGenome + ", transcript " + transcript + ": " + exception.getMessage());
        }
    }

    private List<Sequence> getProteinSequences(REFERENCE_GENOME referenceGenome, List<String> transcripts) {
        if (transcripts.size() == 0) {
            return new ArrayList<>();
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        transcripts.stream().forEach(transcript -> jsonArray.put(transcript));
        try {
            jsonObject.put("ids", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpEntity<String> entity = new HttpEntity<>(jsonObject.toString(), httpHeaders);

        RestTemplate restTemplate = new RestTemplate();
        try {

            ResponseEntity<Sequence[]> response = restTemplate.postForEntity(
                urlService.getEnsemblSequencePOSTUrl(referenceGenome), entity, Sequence[].class);
            return Arrays.asList(response.getBody());
        } catch (HttpClientErrorException exception) {
            throw new BadRequestException("reference genome " + referenceGenome + ", transcripts " + transcripts + ": " + exception.getMessage());
        }
    }

    private TranscriptMatchResultVM pickEnsemblTranscript(REFERENCE_GENOME referenceGenome, List<EnsemblTranscript> availableTranscripts, Sequence sequence) {
        TranscriptMatchResultVM transcriptMatchResultVM = new TranscriptMatchResultVM();

        List<EnsemblTranscript> sameLengthList = availableTranscripts.stream().filter(ensemblTranscript -> ensemblTranscript.getProteinLength() != null && ensemblTranscript.getProteinLength().equals(sequence.getSeq().length())).collect(Collectors.toList());

        List<Sequence> sequences = getProteinSequences(referenceGenome, sameLengthList.stream().map(EnsemblTranscript::getProteinId).collect(Collectors.toList()));
        Optional<Sequence> sequenceSame = sequences.stream().filter(matchedSequence -> matchedSequence.getSeq().equals(sequence.getSeq())).findAny();


        if (sequenceSame.isPresent()) {
            Optional<EnsemblTranscript> ensemblTranscript = getEnsemblTranscriptBySequence(availableTranscripts, sequence);
            transcriptMatchResultVM.setEnsemblTranscript(ensemblTranscript.get());
            transcriptMatchResultVM.setNote("Exact match");
        } else if (sequences.size() > 0) {
            // We should make some comparison with the original sequence for the same length
            sequences.sort(Comparator.comparingInt(s -> getNumOfMismatchSameLengthSequences(sequence.getSeq(), s.getSeq())));
            Sequence pickedSequence = sequences.iterator().next();

            Optional<EnsemblTranscript> ensemblTranscript = getEnsemblTranscriptBySequence(availableTranscripts, pickedSequence);
            transcriptMatchResultVM.setEnsemblTranscript(ensemblTranscript.get());
            transcriptMatchResultVM.setNote("Same length, but mismatch: " + getNumOfMismatchSameLengthSequences(sequence.getSeq(), pickedSequence.getSeq()));
        } else {
            // we want to see whether there is any transcript includes the original sequence
            List<EnsemblTranscript> longerOnes = availableTranscripts.stream().filter(ensemblTranscript -> ensemblTranscript.getProteinLength() != null && ensemblTranscript.getProteinLength() > sequence.getSeq().length()).collect(Collectors.toList());

            List<Sequence> longerSequences = getProteinSequences(referenceGenome, longerOnes.stream().map(EnsemblTranscript::getProteinId).collect(Collectors.toList()));
            List<Sequence> sequencesContains = longerSequences.stream().filter(matchedSequence -> matchedSequence.getSeq().contains(sequence.getSeq())).collect(Collectors.toList());
            sequencesContains.sort((s1, s2) -> s2.getSeq().length() - s1.getSeq().length());

            if (sequencesContains.size() > 0) {
                Sequence pickedSequence = sequencesContains.iterator().next();
                Optional<EnsemblTranscript> ensemblTranscript = getEnsemblTranscriptBySequence(longerOnes, pickedSequence);
                transcriptMatchResultVM.setEnsemblTranscript(ensemblTranscript.get());
                transcriptMatchResultVM.setNote("Longer one found, length: " + pickedSequence.getSeq().length());

            } else {
                transcriptMatchResultVM.setNote("No matched sequence found");
            }
        }
        return transcriptMatchResultVM;
    }

    private Optional<EnsemblTranscript> getEnsemblTranscriptBySequence(List<EnsemblTranscript> availableEnsemblTranscripts, Sequence sequence) {
        return availableEnsemblTranscripts.stream().filter(ensemblTranscript -> {
            if (ensemblTranscript.getProteinId() != null && ensemblTranscript.getProteinId().equals(sequence.getId())) {
                return true;
            } else {
                return false;
            }
        }).findAny();
    }

    private int getNumOfMismatchSameLengthSequences(String reference, String newSequence) {
        int mismatch = 0;
        for (int i = 0; i < reference.length(); i++) {
            char r = reference.charAt(i);
            char n = newSequence.charAt(i);
            if (r != n) {
                mismatch++;
            }
        }
        return mismatch;
    }
}
