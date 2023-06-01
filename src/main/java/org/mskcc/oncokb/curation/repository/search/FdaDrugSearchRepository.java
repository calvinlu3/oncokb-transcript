package org.mskcc.oncokb.curation.repository.search;

import java.util.List;
import java.util.stream.Collectors;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.mskcc.oncokb.curation.domain.FdaDrug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the {@link FdaDrug} entity.
 */
public interface FdaDrugSearchRepository extends ElasticsearchRepository<FdaDrug, Long>, FdaDrugSearchRepositoryInternal {}

interface FdaDrugSearchRepositoryInternal {
    Page<FdaDrug> search(String query, Pageable pageable);
}

class FdaDrugSearchRepositoryInternalImpl implements FdaDrugSearchRepositoryInternal {

    private final ElasticsearchRestTemplate elasticsearchTemplate;

    FdaDrugSearchRepositoryInternalImpl(ElasticsearchRestTemplate elasticsearchTemplate) {
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    @Override
    public Page<FdaDrug> search(String query, Pageable pageable) {
        QueryBuilder queryBuilder = QueryBuilders
            .boolQuery()
            .should(new WildcardQueryBuilder("applicationNumber", query + "*").caseInsensitive(true))
            .should(new WildcardQueryBuilder("brandName", query + "*").caseInsensitive(true))
            .should(new WildcardQueryBuilder("genericName", query + "*").caseInsensitive(true));

        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder()
            .withQuery(queryBuilder)
            .withPageable(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()));

        List<FieldSortBuilder> sortBuilders = new SortToFieldSortBuilderConverter<>(FdaDrug.class).convert(pageable.getSort());
        sortBuilders
            .stream()
            .forEach(sortBuilder -> {
                nativeSearchQueryBuilder.withSort(sortBuilder);
            });

        SearchHits<FdaDrug> searchHits = elasticsearchTemplate.search(nativeSearchQueryBuilder.build(), FdaDrug.class);
        List<FdaDrug> hits = searchHits.map(SearchHit::getContent).stream().collect(Collectors.toList());

        return new PageImpl<>(hits, pageable, searchHits.getTotalHits());
    }
}
