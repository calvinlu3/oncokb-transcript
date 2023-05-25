package org.mskcc.oncokb.curation.repository.search;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure a Mock version of {@link FdaDrugSearchRepository} to test the
 * application without starting Elasticsearch.
 */
@Configuration
public class FdaDrugSearchRepositoryMockConfiguration {

    @MockBean
    private FdaDrugSearchRepository mockFdaDrugSearchRepository;
}
