package org.mskcc.oncokb.curation.web.rest;

import java.util.Optional;
import org.mskcc.oncokb.curation.domain.Article;
import org.mskcc.oncokb.curation.service.ArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link org.mskcc.oncokb.curation.domain.Article}.
 */
@RestController
@RequestMapping("/api")
public class ArticleResource {

    private final Logger log = LoggerFactory.getLogger(ArticleResource.class);

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ArticleService articleService;

    public ArticleResource(ArticleService articleService) {
        this.articleService = articleService;
    }

    /**
     * {@code GET  /articles/:pmid} : get the "pmid" article.
     *
     * @param pmid the pmid of the article to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the article, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/articles/{pmid}")
    public ResponseEntity<Article> getArticle(@PathVariable String pmid) {
        log.debug("REST request to get Article : {}", pmid);
        Optional<Article> article = articleService.findOneByPmid(pmid);
        return ResponseUtil.wrapOrNotFound(article);
    }
}
