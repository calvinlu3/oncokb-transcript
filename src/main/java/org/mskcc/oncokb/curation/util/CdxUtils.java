package org.mskcc.oncokb.curation.util;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mskcc.oncokb.curation.domain.CompanionDiagnosticDevice;
import org.mskcc.oncokb.curation.domain.FdaSubmission;
import org.mskcc.oncokb.curation.domain.FdaSubmissionType;
import org.mskcc.oncokb.curation.service.FdaSubmissionTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CdxUtils {

    private final Logger log = LoggerFactory.getLogger(CdxUtils.class);

    private final String CDX_URL =
        "https://www.fda.gov/medical-devices/in-vitro-diagnostics/list-cleared-or-approved-companion-diagnostic-devices-in-vitro-and-imaging-tools";

    private final Set<String> cdxDeviceFieldNames = Set.of("Device Name", "Trade Name", "Device");

    private final Set<String> cdxNumberFieldNames = Set.of("PMA Number", "HDE Number", "510(K) Number", "De Novo Number");

    @Autowired
    private FdaSubmissionTypeService fdaSubmissionTypeService;

    // Extract relevant information from the FDA CDx page
    public List<CompanionDiagnosticDevice> getCdxListFromHTML() throws IOException {
        // Fetch the html from the fda cdx page
        Document document = Jsoup.connect(CDX_URL).get();

        Element table = document.select("table").first();

        // Extract the table rows
        List<CompanionDiagnosticDevice> companionDiagnosticDevices = new ArrayList<>();
        Element tbody = table.select("tbody").first();
        Integer rowSpanCount = 0;
        for (Element tr : tbody.getElementsByTag("tr")) {
            Elements td = tr.getElementsByTag("td");
            if (rowSpanCount > 0) { // Some cells may span across multiple rows
                rowSpanCount--;
                continue;
            }
            try {
                rowSpanCount = Integer.parseInt(td.first().attributes().get("rowspan")) - 1;
            } catch (NumberFormatException e) {}
            ArrayList<String> tableCells = td.stream().limit(3).map(e -> e.text()).collect(Collectors.toCollection(ArrayList::new));

            // Create CompanionDiagnosticDevice entity
            CompanionDiagnosticDevice cdx = new CompanionDiagnosticDevice();
            cdx.setName(tableCells.get(0));
            Set<String> submissionCodes = Arrays
                .asList(tableCells.get(1).split("\\s"))
                .stream()
                .map(code -> code.trim())
                .filter(code -> code.length() > 0)
                .collect(Collectors.toSet());
            cdx.setFdaSubmissions(getFDASubmissionFromHTML(submissionCodes));
            cdx.setManufacturer(tableCells.get(2));
            companionDiagnosticDevices.add(cdx);
        }
        return companionDiagnosticDevices;
    }

    // Extract the relevant information from the PMA / 510(k) / HDE pages
    public Set<FdaSubmission> getFDASubmissionFromHTML(Set<String> fdaSubmissionCodes) throws IOException {
        // Check if the fda submission codes are valid and purify input
        Set<String> purifiedSubmissionCodes = new HashSet<>();
        for (String code : fdaSubmissionCodes) {
            // Sometimes the PMAs are given as ranges (ie. P990081/S001-S028) and
            // some PMAs in the range don't exists. We should go through this list
            // and skip those that don't exists. (ie P990081/S009 doesn't exists).
            if (code.contains("-")) {
                String primaryPma = code.substring(0, code.indexOf("/"));
                Integer start = Integer.valueOf(code.substring(code.indexOf("/") + 2, code.indexOf("-")));
                Integer end = Integer.valueOf(code.substring(code.indexOf("-") + 2));
                IntStream
                    .rangeClosed(start, end)
                    .forEach(num -> {
                        String pmaString = String.format("%sS%03d", primaryPma, num);
                        purifiedSubmissionCodes.add(pmaString);
                    });
            } else if (code.matches("P\\d{6}\\/S\\d{3}")) { // Matches a PMA number with a supplement
                String primaryPma = code.substring(0, code.indexOf("/"));
                if (!purifiedSubmissionCodes.contains(primaryPma)) {
                    purifiedSubmissionCodes.add(primaryPma);
                }
                purifiedSubmissionCodes.add(code.replace("/", ""));
            } else if (code.matches("DEN\\d{6}\\/K\\d{6}")) { // Special case for DEN
                purifiedSubmissionCodes.add(code.substring(0, code.indexOf("/")));
            } else {
                purifiedSubmissionCodes.add(code.replace("/", ""));
            }
        }

        // Fetch the PMA/510(k)/HDE information from webpage
        Set<FdaSubmission> fdaSubmissions = new HashSet<>();
        for (String code : purifiedSubmissionCodes) {
            String prefix = code.split("[0-9]").length > 0 ? code.split("[0-9]")[0] : null;
            String url = FdaSubmissionUrl.getFullUrl(prefix, code);
            if (url == null) {
                log.warn("Fda submission code is not valid: {}", code);
                continue;
            }

            Document submissionInfoDocument;
            try {
                submissionInfoDocument = Jsoup.connect(url).get();
            } catch (IOException e) {
                log.warn("Failed to fetch fda submission from fda: {}", code);
                continue;
            }

            // The page contains many tables, so we need to find the table containing
            // the relevant information. The PMA/510(K)/HDE tables all have a common 'Date Received'
            // field that we can use to locate the desired table.
            Elements tables = submissionInfoDocument.select("table");
            Element targetTable = null;
            for (Element table : tables) {
                Elements tableHeaders = table.select("> tbody > tr > th");
                if (tableHeaders.isEmpty()) {
                    continue;
                }
                Boolean isTargetTable = tableHeaders.stream().anyMatch(header -> header.text().trim().equals("Date Received"));
                if (isTargetTable) {
                    targetTable = table;
                    break;
                }
            }

            if (targetTable == null) {
                log.warn("No information found for fda submission : {}", code);
                continue;
            }

            Element tbody = targetTable.select("tbody").first();

            FdaSubmission fdaSubmission = new FdaSubmission();

            for (Element tr : tbody.getElementsByTag("tr")) {
                Element th = tr.getElementsByTag("th").first();
                Element td = tr.getElementsByTag("td").first();

                if (th != null) {
                    String header = th.text().trim();
                    String content = td.text().trim();
                    if (this.cdxDeviceFieldNames.stream().anyMatch(header::equalsIgnoreCase)) {
                        fdaSubmission.setDeviceName(content);
                    } else if (this.cdxNumberFieldNames.stream().anyMatch(header::equalsIgnoreCase)) {
                        fdaSubmission.setNumber(content);
                    } else if (header.equalsIgnoreCase("Generic Name")) {
                        fdaSubmission.setGenericName(content);
                    } else if (header.equalsIgnoreCase("Supplement Number")) {
                        fdaSubmission.setSupplementNumber(content);
                    } else if (header.equalsIgnoreCase("Date Received")) {
                        fdaSubmission.setDateReceived(convertDateToInstant(content));
                    } else if (header.equalsIgnoreCase("Decision Date")) {
                        fdaSubmission.setDecisionDate(convertDateToInstant(content));
                    }
                } else {
                    // The Approval Order Statement column does not have the header stored in <th> element
                    // Instead it is a <span> inside <td> element.
                    th = td.children().first();
                    if (th != null) {
                        String header = th.text().trim();
                        if (header.equalsIgnoreCase("Approval Order Statement")) {
                            String approvalOrderStatement = td.textNodes().get(td.textNodes().size() - 1).text();
                            fdaSubmission.setDescription(approvalOrderStatement);
                        }
                    }
                }
            }

            // Add FDASubmissionType
            if (fdaSubmission.getNumber() != null) {
                Optional<FdaSubmissionType> type = fdaSubmissionTypeService.findOneBySubmissionNumberPrefix(fdaSubmission.getNumber());
                if (type.isPresent()) {
                    fdaSubmission.setType(type.get());
                }
            }
            fdaSubmissions.add(fdaSubmission);
        }
        return fdaSubmissions;
    }

    private Instant convertDateToInstant(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("MM/dd/uuuu", Locale.US)).atStartOfDay().toInstant(ZoneOffset.UTC);
    }
}

enum FdaSubmissionUrl {
    PMA("P", "cfpma/pma.cfm"),
    PMN("K", "cfpmn/pmn.cfm"),
    HDE("H", "cfhde/hde.cfm"),
    DEN("DEN", "cfpmn/denovo.cfm");

    String prefix;
    String urlSuffix;

    FdaSubmissionUrl(String prefix, String urlSuffix) {
        this.prefix = prefix;
        this.urlSuffix = urlSuffix;
    }

    public static String getFullUrl(String prefix, String idParam) {
        for (FdaSubmissionUrl fdaSubmissionUrl : FdaSubmissionUrl.values()) {
            if (fdaSubmissionUrl.prefix.equalsIgnoreCase(prefix)) {
                return "https://www.accessdata.fda.gov/scripts/cdrh/cfdocs/" + fdaSubmissionUrl.urlSuffix + "?id=" + idParam;
            }
        }
        return null;
    }
}
