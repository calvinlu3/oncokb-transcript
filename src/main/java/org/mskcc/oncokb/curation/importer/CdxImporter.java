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
import org.mskcc.oncokb.curation.domain.FdaDrug;
import org.mskcc.oncokb.curation.domain.FdaSubmission;
import org.mskcc.oncokb.curation.domain.Gene;
import org.mskcc.oncokb.curation.domain.SpecimenType;
import org.mskcc.oncokb.curation.service.AlterationService;
import org.mskcc.oncokb.curation.service.CancerTypeService;
import org.mskcc.oncokb.curation.service.CompanionDiagnosticDeviceService;
import org.mskcc.oncokb.curation.service.DrugService;
import org.mskcc.oncokb.curation.service.FdaDrugService;
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

    @Autowired
    private FdaDrugService fdaDrugService;

    private final Logger log = LoggerFactory.getLogger(Importer.class);

    public void importMain() throws IOException {
        List<List<String>> parsedCdxContent = readInitialCdxFile();
        if (parsedCdxContent == null) {
            return;
        }

        List<String> headers = parsedCdxContent.get(0);
        List<DeviceUsageIndication> deviceUsageIndications = new ArrayList<>();

        for (int i = 0; i < parsedCdxContent.size(); i++) {
            if (i == 0) {
                continue; // Skipping header row
            }

            List<String> row = parsedCdxContent.get(i);
            CompanionDiagnosticDevice cdx = null;
            FdaSubmission fdaSubmission = null;
            List<Drug> drugs = new ArrayList<>();
            // List<FdaDrug> fdaDrugs = new ArrayList<>();
            Gene gene = null;
            List<Alteration> alterations = new ArrayList<>();
            CancerType cancerType = null;

            // Parse row
            for (int colIndex = 0; colIndex < row.size(); colIndex++) {
                String columnValue = row.get(colIndex).trim();
                Pattern pattern = null;
                Matcher matcher = null;
                Boolean shouldSkipRow = true;
                switch (colIndex) {
                    case 0:
                        pattern = Pattern.compile("^(.*)\\((.*?)\\)");
                        matcher = pattern.matcher(columnValue);
                        if (matcher.find() && matcher.groupCount() == 2) {
                            String cdxName = matcher.group(1);
                            String cdxManufacturer = matcher.group(2);
                            cdx =
                                companionDiagnosticDeviceService
                                    .findByNameAndManufacturer(cdxName, cdxManufacturer)
                                    .stream()
                                    .findFirst()
                                    .orElse(new CompanionDiagnosticDevice());
                            cdx.setName(cdxName);
                            cdx.setManufacturer(cdxManufacturer);
                        } else {
                            log.error("Could not parse header '{}' with value '{}'", headers.get(colIndex), row.get(colIndex));
                            shouldSkipRow = true;
                        }
                        break;
                    // case 2:
                    //     String[] fdaDrugStrings = { columnValue };
                    //     if (columnValue.contains("+")) {    // Combination drug
                    //         fdaDrugStrings = columnValue.split("+");
                    //     }
                    //     for (String fdaDrugString: fdaDrugStrings) {
                    //         pattern = Pattern.compile("^(.*)\\((.*?)\\)(.*)");
                    //         matcher = pattern.matcher(columnValue);
                    //         if (matcher.find() && matcher.groupCount() == 2) {
                    //             String brandName = matcher.group(1).trim();
                    //             String genericName = matcher.group(2).trim();
                    //             String applicationNumber = matcher.group(3).replaceAll("\\s", "");
                    //             FdaDrug fdaDrug = fdaDrugService.findOneByApplicationNumber(applicationNumber).orElse(new FdaDrug());
                    //             fdaDrug.setBrandName(brandName);
                    //             fdaDrug.setGenericName(genericName);
                    //             fdaDrug.setApplicationNumber(applicationNumber);
                    //             fdaDrugs.add(fdaDrug);
                    //         } else {
                    //             log.error("Could not parse header '{}' with value '{}'", headers.get(colIndex), row.get(colIndex));
                    //             shouldSkipRow = true;
                    //             break;
                    //         }
                    //     }
                    //     break;
                    case 6:
                        pattern = Pattern.compile("^([A-Za-z\\d]+)\\/([A-Za-z\\d]+)");
                        matcher = pattern.matcher(columnValue);
                        if (matcher.find() && matcher.groupCount() == 2) {
                            String number = matcher.group(1);
                            String supplementNumber = matcher.group(2);
                            fdaSubmission =
                                fdaSubmissionService.findByNumberAndSupplementNumber(number, supplementNumber).orElse(new FdaSubmission());
                            fdaSubmission.setNumber(number);
                            fdaSubmission.setSupplementNumber(supplementNumber);
                        }
                        break;
                    case 7:
                        Instant fdaSubmissionDate = cdxUtils.convertDateToInstant(columnValue);
                        if (fdaSubmission != null) {
                            fdaSubmission.setDecisionDate(fdaSubmissionDate);
                        }
                        break;
                    case 8:
                        if (cdx != null) {
                            cdx.setIndicationDetails(columnValue);
                        }
                        break;
                    case 9:
                        Optional<CancerType> optionalCancerType = cancerTypeService.findOneByCode(columnValue);
                        if (optionalCancerType.isPresent()) {
                            cancerType = optionalCancerType.get();
                        } else {
                            shouldSkipRow = true;
                            log.error("Cannot find cancer type with code '{}'", columnValue);
                        }
                        break;
                    case 10:
                        String[] drugStrings = { columnValue };
                        if (columnValue.contains("+")) { // Combination drug
                            drugStrings = columnValue.split("+");
                        }
                        for (String drugString : drugStrings) {
                            drugString = drugString.trim();
                            Drug drug = drugService.findByName(drugString).orElse(null);
                            if (drug == null) {
                                log.error("Could not find drug '{}'", drugString);
                                shouldSkipRow = true;
                                break;
                            }
                            drugs.add(drug);
                        }
                        break;
                    case 11:
                        Optional<Gene> optionalGene = geneService.findGeneByHugoSymbol(columnValue);
                        if (optionalGene.isPresent()) {
                            gene = optionalGene.get();
                        } else {
                            shouldSkipRow = true;
                            log.error("Cannot find gene '{}'", columnValue);
                        }
                        break;
                    case 12:
                        if (gene == null || gene.getId() == null) {
                            shouldSkipRow = true;
                            break;
                        }
                        Long geneId = gene.getId();
                        String[] alterationStrings = { columnValue };
                        if (columnValue.contains(",")) {
                            alterationStrings = columnValue.split(",");
                        }
                        for (String alterationString : alterationStrings) {
                            alterationString = alterationString.trim();
                            Optional<Alteration> optionalAlteration = alterationService.findOneByGeneIdAndAlterationName(
                                geneId,
                                alterationString
                            );
                            if (optionalAlteration.isPresent()) {
                                alterations.add(optionalAlteration.get());
                            } else {
                                shouldSkipRow = true;
                                log.error("Cannot find alteration '{}' for gene '{}'", alterationString, gene.getHugoSymbol());
                                break;
                            }
                        }
                        break;
                    case 13:
                        Optional<SpecimenType> optionalSpecimenType = specimenTypeService.findOneByType(columnValue);
                        if (optionalSpecimenType.isPresent() && cdx != null) {
                            cdx.addSpecimenType(optionalSpecimenType.get());
                        }
                    default:
                        break;
                }

                if (shouldSkipRow) {
                    log.error("Error processing row {}. Skipping...", i);
                    break;
                }
            }

            // Create DeviceUsageIndication
            DeviceUsageIndication deviceUsageIndication = new DeviceUsageIndication();
            deviceUsageIndication.setCancerType(cancerType);
            deviceUsageIndication.setFdaSubmission(fdaSubmission);
            // Todo: set other fields after updating entity relationships
            deviceUsageIndications.add(deviceUsageIndication);
        }
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
