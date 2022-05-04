package org.mskcc.oncokb.curation.repository.search;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure a Mock version of {@link AlterationSearchRepository} to test the
 * application without starting Elasticsearch.
 */
@Configuration
public class AlterationSearchRepositoryMockConfiguration {

    @MockBean
    private AlterationSearchRepository mockAlterationSearchRepository;
}
