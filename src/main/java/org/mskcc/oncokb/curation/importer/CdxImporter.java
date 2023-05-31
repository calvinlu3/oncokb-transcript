package org.mskcc.oncokb.curation.importer;

import static org.mskcc.oncokb.curation.util.FileUtils.readDelimitedLinesStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mskcc.oncokb.curation.domain.Alteration;
import org.mskcc.oncokb.curation.domain.CancerType;
import org.mskcc.oncokb.curation.domain.CompanionDiagnosticDevice;
import org.mskcc.oncokb.curation.domain.DeviceUsageIndication;
import org.mskcc.oncokb.curation.domain.Drug;
import org.mskcc.oncokb.curation.domain.FdaSubmission;
import org.mskcc.oncokb.curation.domain.Gene;
import org.mskcc.oncokb.curation.domain.SpecimenType;
import org.mskcc.oncokb.curation.service.AlterationService;
import org.mskcc.oncokb.curation.service.CancerTypeService;
import org.mskcc.oncokb.curation.service.CompanionDiagnosticDeviceService;
import org.mskcc.oncokb.curation.service.DeviceUsageIndicationService;
import org.mskcc.oncokb.curation.service.DrugService;
import org.mskcc.oncokb.curation.service.FdaSubmissionService;
import org.mskcc.oncokb.curation.service.GeneService;
import org.mskcc.oncokb.curation.service.SpecimenTypeService;
import org.mskcc.oncokb.curation.util.CdxUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CdxImporter {

    @Autowired
    private CdxUtils cdxUtils;

    @Autowired
    private DeviceUsageIndicationService deviceUsageIndicationService;

    @Autowired
    private CompanionDiagnosticDeviceService companionDiagnosticDeviceService;

    @Autowired
    private FdaSubmissionService fdaSubmissionService;

    @Autowired
    private SpecimenTypeService specimenTypeService;

    @Autowired
    private GeneService geneService;

    @Autowired
    private AlterationService alterationService;

    @Autowired
    private CancerTypeService cancerTypeService;

    @Autowired
    private DrugService drugService;

    private final Logger log = LoggerFactory.getLogger(Importer.class);

    public void importCdxMain() throws IOException {
        List<List<String>> parsedCdxContent = readInitialCdxFile();
        if (parsedCdxContent == null) {
            return;
        }

        List<String> headers = parsedCdxContent.get(0);

        for (int i = 0; i < parsedCdxContent.size(); i++) {
            if (i == 0) {
                continue; // Skipping header row
            }

            List<String> row = parsedCdxContent.get(i);
            CompanionDiagnosticDevice cdx = null;
            FdaSubmission fdaSubmission = null;
            List<Drug> drugs = new ArrayList<>();
            Gene gene = null;
            List<Alteration> alterations = new ArrayList<>();
            CancerType cancerType = null;

            // Parse row
            for (int colIndex = 0; colIndex < row.size(); colIndex++) {
                String columnValue = row.get(colIndex).trim();
                try {
                    switch (colIndex) {
                        case 0:
                            cdx = parseCdxNameColumn(columnValue);
                            break;
                        case 6:
                            fdaSubmission = parseFdaSubmissionColumn(columnValue);
                            break;
                        case 7:
                            Instant fdaSubmissionDate = cdxUtils.convertDateToInstant(columnValue);
                            fdaSubmission.setDecisionDate(fdaSubmissionDate);
                            break;
                        case 8:
                            cdx.setIndicationDetails(columnValue);
                            break;
                        case 9:
                            cancerType = parseCancerTypeColumn(columnValue);
                            break;
                        case 10:
                            drugs = parseDrugColumn(columnValue);
                            break;
                        case 11:
                            Optional<Gene> optionalGene = geneService.findGeneByHugoSymbol(columnValue);
                            if (optionalGene.isPresent()) {
                                gene = optionalGene.get();
                            } else {
                                throw new IllegalArgumentException("Could not find gene " + columnValue);
                            }
                            break;
                        case 12:
                            alterations = parseAlterationColumn(columnValue, gene);
                            break;
                        case 13:
                            Optional<SpecimenType> optionalSpecimenType = specimenTypeService.findOneByType(columnValue);
                            if (optionalSpecimenType.isPresent()) {
                                cdx.addSpecimenType(optionalSpecimenType.get());
                            }
                        default:
                            break;
                    }
                } catch (IllegalArgumentException e) {
                    String message = e.getMessage();
                    if (e.getMessage() == null) {
                        message = String.format("Cannot parse column '{}' with value '{}'", headers.get(colIndex), columnValue);
                    }
                    log.error(message);
                    log.error("Issue processing row {}, skipping...", i);
                    break;
                }
            }

            saveCdxInformation(cdx, fdaSubmission, cancerType, drugs, alterations);
            log.info("Successfully imported row {}", i);
            break;
        }
    }

    private void saveCdxInformation(
        CompanionDiagnosticDevice cdx,
        FdaSubmission fdaSubmission,
        CancerType cancerType,
        List<Drug> drugs,
        List<Alteration> alterations
    ) {
        fdaSubmissionService.save(fdaSubmission);
        cdx.addFdaSubmission(fdaSubmission);
        companionDiagnosticDeviceService.save(cdx);

        // Create DeviceUsageIndication
        DeviceUsageIndication deviceUsageIndication = new DeviceUsageIndication();
        deviceUsageIndication.setCancerType(cancerType);
        deviceUsageIndication.setFdaSubmission(fdaSubmission);
        drugs.stream().forEach(drug -> deviceUsageIndication.addDrug(drug));
        alterations.stream().forEach(alteration -> deviceUsageIndication.addAlteration(alteration));
        deviceUsageIndicationService.save(deviceUsageIndication);
    }

    private CompanionDiagnosticDevice parseCdxNameColumn(String columnValue) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile("^(.*)\\((.*?)\\)");
        Matcher matcher = pattern.matcher(columnValue);
        if (matcher.find() && matcher.group(1) != null && matcher.group(2) != null) {
            String cdxName = matcher.group(1);
            String cdxManufacturer = matcher.group(2);
            CompanionDiagnosticDevice cdx = companionDiagnosticDeviceService
                .findByNameAndManufacturer(cdxName, cdxManufacturer)
                .stream()
                .findFirst()
                .orElse(new CompanionDiagnosticDevice());
            cdx.setName(cdxName);
            cdx.setManufacturer(cdxManufacturer);
            return cdx;
        } else {
            throw new IllegalArgumentException();
        }
    }

    private FdaSubmission parseFdaSubmissionColumn(String columnValue) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile("^([A-Za-z\\d]+)(\\/([A-Za-z\\d]+))?");
        Matcher matcher = pattern.matcher(columnValue);
        if (matcher.find() && matcher.group(1) != null) {
            String number = matcher.group(1);
            String supplementNumber = Optional.ofNullable(matcher.group(3)).orElse("");
            FdaSubmission fdaSubmission = fdaSubmissionService
                .findByNumberAndSupplementNumber(number, supplementNumber)
                .orElse(new FdaSubmission());
            fdaSubmission.setNumber(number);
            fdaSubmission.setSupplementNumber(supplementNumber);
            return fdaSubmission;
        } else {
            throw new IllegalArgumentException();
        }
    }

    private CancerType parseCancerTypeColumn(String columnValue) throws IllegalArgumentException {
        return cancerTypeService.findOneByCode(columnValue).orElseThrow(IllegalArgumentException::new);
    }

    private List<Drug> parseDrugColumn(String columnValue) throws IllegalArgumentException {
        List<Drug> drugs = new ArrayList<>();
        String[] drugStrings = { columnValue };
        if (columnValue.contains("+")) { // Combination drug
            drugStrings = columnValue.split("+");
        }
        for (String drugString : drugStrings) {
            drugString = drugString.trim();
            Drug drug = drugService.findByName(drugString).orElse(null);
            if (drug != null) {
                drugs.add(drug);
            } else {
                throw new IllegalArgumentException("Could not find drug " + drugString);
            }
        }
        return drugs;
    }

    private List<Alteration> parseAlterationColumn(String columnValue, Gene gene) throws IllegalArgumentException {
        if (gene == null || gene.getId() == null) {
            throw new IllegalArgumentException();
        }
        List<Alteration> alterations = new ArrayList<>();
        Long geneId = gene.getId();
        String[] alterationStrings = { columnValue };
        if (columnValue.contains(",")) {
            alterationStrings = columnValue.split(",");
        }
        for (String alterationString : alterationStrings) {
            alterationString = alterationString.trim();
            Optional<Alteration> optionalAlteration = alterationService.findOneByGeneIdAndAlterationName(geneId, alterationString);
            if (optionalAlteration.isPresent()) {
                alterations.add(optionalAlteration.get());
            } else {
                throw new IllegalArgumentException("Cannot find alteration " + alterationString + " for gene " + gene.getHugoSymbol());
            }
        }
        return alterations;
    }

    private List<List<String>> readInitialCdxFile() throws IOException {
        URL cdxFileUrl = getClass().getClassLoader().getResource("data/initial_cdx_data.tsv");
        if (cdxFileUrl == null) {
            log.error("Cannot find CDx file");
            return null;
        }
        InputStream is = cdxFileUrl.openStream();
        return readDelimitedLinesStream(is, "\\t", true);
    }
}
