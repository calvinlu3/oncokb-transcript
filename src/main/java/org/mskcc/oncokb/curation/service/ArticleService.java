package org.mskcc.oncokb.curation.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.mskcc.oncokb.curation.domain.Article;
import org.mskcc.oncokb.curation.repository.ArticleRepository;
import org.mskcc.oncokb.curation.util.NcbiEUtils;
import org.mskcc.oncokb.meta.model.application.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Article}.
 */
@Service
@Transactional
public class ArticleService {

    private final Logger log = LoggerFactory.getLogger(ArticleService.class);

    private final ArticleRepository articleRepository;

    private final NcbiEUtils ncbiEUtils;

    public ArticleService(ArticleRepository articleRepository, NcbiEUtils ncbiEUtils) {
        this.articleRepository = articleRepository;
        this.ncbiEUtils = ncbiEUtils;
    }

    /**
     * Save a article.
     *
     * @param article the entity to save.
     * @return the persisted entity.
     */
    public Article save(Article article) {
        log.debug("Request to save Article : {}", article);
        return articleRepository.save(article);
    }

    /**
     * Get one article by pmid.
     *
     * @param pmid the article pmid
     * @return the entity.
     */
    @Transactional
    public Optional<Article> findOneByPmid(String pmid) {
        log.debug("Request to get Article : {}", pmid);

        Optional<Article> article = articleRepository.findOneByPmid(pmid);
        if (!article.isPresent()) {
            Set<Article> articles = ncbiEUtils.readPubmedArticles(Stream.of(pmid).collect(Collectors.toCollection(HashSet::new)));
            if (!articles.isEmpty()) {
                Article savedArticle = articleRepository.save(articles.iterator().next());
                return Optional.of(savedArticle);
            }
        }

        return article;
    }
}
