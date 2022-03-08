package org.mskcc.oncokb.curation.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.oncokb.curation.config.ApplicationProperties;
import org.mskcc.oncokb.curation.domain.Article;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public final class NcbiEUtils {

    private static final String URL_NCBI_EUTILS = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";

    private final Logger log = LoggerFactory.getLogger(NcbiEUtils.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ApplicationProperties applicationProperties;

    public NcbiEUtils(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public Set<Article> readPubmedArticles(Set<String> pmids) {
        String apiKey = this.applicationProperties.getNcbiApiKey();

        Set<Article> results = new HashSet<>();

        if (pmids == null) {
            return results;
        }

        pmids = purifyInput(pmids);

        if (pmids.isEmpty()) {
            return results;
        }

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Date date = new Date();

        // Fetch article information from NCBI api
        String url =
            URL_NCBI_EUTILS +
            "esummary.fcgi?api_key=" +
            apiKey +
            "&db=pubmed&retmode=json&id=" +
            StringUtils.join(new ArrayList<>(pmids), ",");
        log.debug("Making a NCBI request at " + dateFormat.format(date) + " " + url);
        RestTemplate restTemplate = new RestTemplate();
        String json = restTemplate.getForObject(url, String.class);

        Map result = null;

        try {
            Map map = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            result = (Map) (map.get("result"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (result == null) {
            return results;
        }
        for (String pmid : pmids) {
            Article article = new Article();
            article.setPmid(pmid);
            Map articleInfo = (Map) (result.get(pmid));

            if (articleInfo != null) {
                String pubdate = (String) (articleInfo.get("pubdate"));
                article.setPubDate(pubdate);

                if (articleInfo.get("authors") != null && !articleInfo.get("authors").getClass().equals(String.class)) {
                    List<Map<String, String>> authors = (List) (articleInfo.get("authors"));
                    article.setAuthors(formatAuthors(authors));
                } else {
                    article.setAuthors(null);
                }

                String title = (String) (articleInfo.get("title"));

                if (title == null) {
                    continue;
                }

                article.setTitle(title);

                String volume = (String) (articleInfo.get("volume"));
                article.setVolume(volume);

                String issue = (String) (articleInfo.get("issue"));
                article.setIssue(issue);

                String pages = (String) (articleInfo.get("pages"));
                article.setPages(pages);

                String fulljournalname = (String) (articleInfo.get("fulljournalname"));
                article.setJournal(fulljournalname);

                results.add(article);
            } else {
                log.debug("No article info for {}", pmid);
            }
        }

        return results;
    }

    private String formatAuthors(List<Map<String, String>> authors) {
        StringBuilder sb = new StringBuilder();
        if (authors != null && authors.size() > 0) {
            sb.append(authors.get(0).get("name"));
            if (authors.size() > 1) {
                sb.append(" et al");
            }
        }
        return sb.toString();
    }

    private Set<String> purifyInput(Set<String> pmids) {
        Set<String> purified = new HashSet<>();
        for (String pmid : pmids) {
            if (pmid == null) continue;

            pmid = pmid.trim();

            if (pmid.isEmpty()) continue;

            if (!StringUtils.isNumeric(pmid)) {
                log.debug("{} is not a number", pmid);
            } else {
                purified.add(pmid);
            }
        }
        return purified;
    }
}
