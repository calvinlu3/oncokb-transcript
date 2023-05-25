package org.mskcc.oncokb.curation.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mskcc.oncokb.curation.IntegrationTest;
import org.mskcc.oncokb.curation.domain.FdaDrug;
import org.mskcc.oncokb.curation.repository.FdaDrugRepository;
import org.mskcc.oncokb.curation.repository.search.FdaDrugSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link FdaDrugResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class FdaDrugResourceIT {

    private static final String DEFAULT_APPLICATION_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_APPLICATION_NUMBER = "BBBBBBBBBB";

    private static final String DEFAULT_BRAND_NAME = "AAAAAAAAAA";
    private static final String UPDATED_BRAND_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_GENERIC_NAME = "AAAAAAAAAA";
    private static final String UPDATED_GENERIC_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/fda-drugs";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/fda-drugs";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private FdaDrugRepository fdaDrugRepository;

    /**
     * This repository is mocked in the org.mskcc.oncokb.curation.repository.search test package.
     *
     * @see org.mskcc.oncokb.curation.repository.search.FdaDrugSearchRepositoryMockConfiguration
     */
    @Autowired
    private FdaDrugSearchRepository mockFdaDrugSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restFdaDrugMockMvc;

    private FdaDrug fdaDrug;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FdaDrug createEntity(EntityManager em) {
        FdaDrug fdaDrug = new FdaDrug()
            .applicationNumber(DEFAULT_APPLICATION_NUMBER)
            .brandName(DEFAULT_BRAND_NAME)
            .genericName(DEFAULT_GENERIC_NAME);
        return fdaDrug;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FdaDrug createUpdatedEntity(EntityManager em) {
        FdaDrug fdaDrug = new FdaDrug()
            .applicationNumber(UPDATED_APPLICATION_NUMBER)
            .brandName(UPDATED_BRAND_NAME)
            .genericName(UPDATED_GENERIC_NAME);
        return fdaDrug;
    }

    @BeforeEach
    public void initTest() {
        fdaDrug = createEntity(em);
    }

    @Test
    @Transactional
    void createFdaDrug() throws Exception {
        int databaseSizeBeforeCreate = fdaDrugRepository.findAll().size();
        // Create the FdaDrug
        restFdaDrugMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(fdaDrug))
            )
            .andExpect(status().isCreated());

        // Validate the FdaDrug in the database
        List<FdaDrug> fdaDrugList = fdaDrugRepository.findAll();
        assertThat(fdaDrugList).hasSize(databaseSizeBeforeCreate + 1);
        FdaDrug testFdaDrug = fdaDrugList.get(fdaDrugList.size() - 1);
        assertThat(testFdaDrug.getApplicationNumber()).isEqualTo(DEFAULT_APPLICATION_NUMBER);
        assertThat(testFdaDrug.getBrandName()).isEqualTo(DEFAULT_BRAND_NAME);
        assertThat(testFdaDrug.getGenericName()).isEqualTo(DEFAULT_GENERIC_NAME);

        // Validate the FdaDrug in Elasticsearch
        verify(mockFdaDrugSearchRepository, times(1)).save(testFdaDrug);
    }

    @Test
    @Transactional
    void createFdaDrugWithExistingId() throws Exception {
        // Create the FdaDrug with an existing ID
        fdaDrug.setId(1L);

        int databaseSizeBeforeCreate = fdaDrugRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restFdaDrugMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(fdaDrug))
            )
            .andExpect(status().isBadRequest());

        // Validate the FdaDrug in the database
        List<FdaDrug> fdaDrugList = fdaDrugRepository.findAll();
        assertThat(fdaDrugList).hasSize(databaseSizeBeforeCreate);

        // Validate the FdaDrug in Elasticsearch
        verify(mockFdaDrugSearchRepository, times(0)).save(fdaDrug);
    }

    @Test
    @Transactional
    void checkApplicationNumberIsRequired() throws Exception {
        int databaseSizeBeforeTest = fdaDrugRepository.findAll().size();
        // set the field null
        fdaDrug.setApplicationNumber(null);

        // Create the FdaDrug, which fails.

        restFdaDrugMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(fdaDrug))
            )
            .andExpect(status().isBadRequest());

        List<FdaDrug> fdaDrugList = fdaDrugRepository.findAll();
        assertThat(fdaDrugList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllFdaDrugs() throws Exception {
        // Initialize the database
        fdaDrugRepository.saveAndFlush(fdaDrug);

        // Get all the fdaDrugList
        restFdaDrugMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(fdaDrug.getId().intValue())))
            .andExpect(jsonPath("$.[*].applicationNumber").value(hasItem(DEFAULT_APPLICATION_NUMBER)))
            .andExpect(jsonPath("$.[*].brandName").value(hasItem(DEFAULT_BRAND_NAME)))
            .andExpect(jsonPath("$.[*].genericName").value(hasItem(DEFAULT_GENERIC_NAME)));
    }

    @Test
    @Transactional
    void getFdaDrug() throws Exception {
        // Initialize the database
        fdaDrugRepository.saveAndFlush(fdaDrug);

        // Get the fdaDrug
        restFdaDrugMockMvc
            .perform(get(ENTITY_API_URL_ID, fdaDrug.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(fdaDrug.getId().intValue()))
            .andExpect(jsonPath("$.applicationNumber").value(DEFAULT_APPLICATION_NUMBER))
            .andExpect(jsonPath("$.brandName").value(DEFAULT_BRAND_NAME))
            .andExpect(jsonPath("$.genericName").value(DEFAULT_GENERIC_NAME));
    }

    @Test
    @Transactional
    void getNonExistingFdaDrug() throws Exception {
        // Get the fdaDrug
        restFdaDrugMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewFdaDrug() throws Exception {
        // Initialize the database
        fdaDrugRepository.saveAndFlush(fdaDrug);

        int databaseSizeBeforeUpdate = fdaDrugRepository.findAll().size();

        // Update the fdaDrug
        FdaDrug updatedFdaDrug = fdaDrugRepository.findById(fdaDrug.getId()).get();
        // Disconnect from session so that the updates on updatedFdaDrug are not directly saved in db
        em.detach(updatedFdaDrug);
        updatedFdaDrug.applicationNumber(UPDATED_APPLICATION_NUMBER).brandName(UPDATED_BRAND_NAME).genericName(UPDATED_GENERIC_NAME);

        restFdaDrugMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedFdaDrug.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedFdaDrug))
            )
            .andExpect(status().isOk());

        // Validate the FdaDrug in the database
        List<FdaDrug> fdaDrugList = fdaDrugRepository.findAll();
        assertThat(fdaDrugList).hasSize(databaseSizeBeforeUpdate);
        FdaDrug testFdaDrug = fdaDrugList.get(fdaDrugList.size() - 1);
        assertThat(testFdaDrug.getApplicationNumber()).isEqualTo(UPDATED_APPLICATION_NUMBER);
        assertThat(testFdaDrug.getBrandName()).isEqualTo(UPDATED_BRAND_NAME);
        assertThat(testFdaDrug.getGenericName()).isEqualTo(UPDATED_GENERIC_NAME);

        // Validate the FdaDrug in Elasticsearch
        verify(mockFdaDrugSearchRepository).save(testFdaDrug);
    }

    @Test
    @Transactional
    void putNonExistingFdaDrug() throws Exception {
        int databaseSizeBeforeUpdate = fdaDrugRepository.findAll().size();
        fdaDrug.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFdaDrugMockMvc
            .perform(
                put(ENTITY_API_URL_ID, fdaDrug.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(fdaDrug))
            )
            .andExpect(status().isBadRequest());

        // Validate the FdaDrug in the database
        List<FdaDrug> fdaDrugList = fdaDrugRepository.findAll();
        assertThat(fdaDrugList).hasSize(databaseSizeBeforeUpdate);

        // Validate the FdaDrug in Elasticsearch
        verify(mockFdaDrugSearchRepository, times(0)).save(fdaDrug);
    }

    @Test
    @Transactional
    void putWithIdMismatchFdaDrug() throws Exception {
        int databaseSizeBeforeUpdate = fdaDrugRepository.findAll().size();
        fdaDrug.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFdaDrugMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(fdaDrug))
            )
            .andExpect(status().isBadRequest());

        // Validate the FdaDrug in the database
        List<FdaDrug> fdaDrugList = fdaDrugRepository.findAll();
        assertThat(fdaDrugList).hasSize(databaseSizeBeforeUpdate);

        // Validate the FdaDrug in Elasticsearch
        verify(mockFdaDrugSearchRepository, times(0)).save(fdaDrug);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamFdaDrug() throws Exception {
        int databaseSizeBeforeUpdate = fdaDrugRepository.findAll().size();
        fdaDrug.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFdaDrugMockMvc
            .perform(
                put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(fdaDrug))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the FdaDrug in the database
        List<FdaDrug> fdaDrugList = fdaDrugRepository.findAll();
        assertThat(fdaDrugList).hasSize(databaseSizeBeforeUpdate);

        // Validate the FdaDrug in Elasticsearch
        verify(mockFdaDrugSearchRepository, times(0)).save(fdaDrug);
    }

    @Test
    @Transactional
    void partialUpdateFdaDrugWithPatch() throws Exception {
        // Initialize the database
        fdaDrugRepository.saveAndFlush(fdaDrug);

        int databaseSizeBeforeUpdate = fdaDrugRepository.findAll().size();

        // Update the fdaDrug using partial update
        FdaDrug partialUpdatedFdaDrug = new FdaDrug();
        partialUpdatedFdaDrug.setId(fdaDrug.getId());

        partialUpdatedFdaDrug.applicationNumber(UPDATED_APPLICATION_NUMBER).genericName(UPDATED_GENERIC_NAME);

        restFdaDrugMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFdaDrug.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedFdaDrug))
            )
            .andExpect(status().isOk());

        // Validate the FdaDrug in the database
        List<FdaDrug> fdaDrugList = fdaDrugRepository.findAll();
        assertThat(fdaDrugList).hasSize(databaseSizeBeforeUpdate);
        FdaDrug testFdaDrug = fdaDrugList.get(fdaDrugList.size() - 1);
        assertThat(testFdaDrug.getApplicationNumber()).isEqualTo(UPDATED_APPLICATION_NUMBER);
        assertThat(testFdaDrug.getBrandName()).isEqualTo(DEFAULT_BRAND_NAME);
        assertThat(testFdaDrug.getGenericName()).isEqualTo(UPDATED_GENERIC_NAME);
    }

    @Test
    @Transactional
    void fullUpdateFdaDrugWithPatch() throws Exception {
        // Initialize the database
        fdaDrugRepository.saveAndFlush(fdaDrug);

        int databaseSizeBeforeUpdate = fdaDrugRepository.findAll().size();

        // Update the fdaDrug using partial update
        FdaDrug partialUpdatedFdaDrug = new FdaDrug();
        partialUpdatedFdaDrug.setId(fdaDrug.getId());

        partialUpdatedFdaDrug.applicationNumber(UPDATED_APPLICATION_NUMBER).brandName(UPDATED_BRAND_NAME).genericName(UPDATED_GENERIC_NAME);

        restFdaDrugMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFdaDrug.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedFdaDrug))
            )
            .andExpect(status().isOk());

        // Validate the FdaDrug in the database
        List<FdaDrug> fdaDrugList = fdaDrugRepository.findAll();
        assertThat(fdaDrugList).hasSize(databaseSizeBeforeUpdate);
        FdaDrug testFdaDrug = fdaDrugList.get(fdaDrugList.size() - 1);
        assertThat(testFdaDrug.getApplicationNumber()).isEqualTo(UPDATED_APPLICATION_NUMBER);
        assertThat(testFdaDrug.getBrandName()).isEqualTo(UPDATED_BRAND_NAME);
        assertThat(testFdaDrug.getGenericName()).isEqualTo(UPDATED_GENERIC_NAME);
    }

    @Test
    @Transactional
    void patchNonExistingFdaDrug() throws Exception {
        int databaseSizeBeforeUpdate = fdaDrugRepository.findAll().size();
        fdaDrug.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFdaDrugMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, fdaDrug.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(fdaDrug))
            )
            .andExpect(status().isBadRequest());

        // Validate the FdaDrug in the database
        List<FdaDrug> fdaDrugList = fdaDrugRepository.findAll();
        assertThat(fdaDrugList).hasSize(databaseSizeBeforeUpdate);

        // Validate the FdaDrug in Elasticsearch
        verify(mockFdaDrugSearchRepository, times(0)).save(fdaDrug);
    }

    @Test
    @Transactional
    void patchWithIdMismatchFdaDrug() throws Exception {
        int databaseSizeBeforeUpdate = fdaDrugRepository.findAll().size();
        fdaDrug.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFdaDrugMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(fdaDrug))
            )
            .andExpect(status().isBadRequest());

        // Validate the FdaDrug in the database
        List<FdaDrug> fdaDrugList = fdaDrugRepository.findAll();
        assertThat(fdaDrugList).hasSize(databaseSizeBeforeUpdate);

        // Validate the FdaDrug in Elasticsearch
        verify(mockFdaDrugSearchRepository, times(0)).save(fdaDrug);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamFdaDrug() throws Exception {
        int databaseSizeBeforeUpdate = fdaDrugRepository.findAll().size();
        fdaDrug.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFdaDrugMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(fdaDrug))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the FdaDrug in the database
        List<FdaDrug> fdaDrugList = fdaDrugRepository.findAll();
        assertThat(fdaDrugList).hasSize(databaseSizeBeforeUpdate);

        // Validate the FdaDrug in Elasticsearch
        verify(mockFdaDrugSearchRepository, times(0)).save(fdaDrug);
    }

    @Test
    @Transactional
    void deleteFdaDrug() throws Exception {
        // Initialize the database
        fdaDrugRepository.saveAndFlush(fdaDrug);

        int databaseSizeBeforeDelete = fdaDrugRepository.findAll().size();

        // Delete the fdaDrug
        restFdaDrugMockMvc
            .perform(delete(ENTITY_API_URL_ID, fdaDrug.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<FdaDrug> fdaDrugList = fdaDrugRepository.findAll();
        assertThat(fdaDrugList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the FdaDrug in Elasticsearch
        verify(mockFdaDrugSearchRepository, times(1)).deleteById(fdaDrug.getId());
    }

    @Test
    @Transactional
    void searchFdaDrug() throws Exception {
        // Configure the mock search repository
        // Initialize the database
        fdaDrugRepository.saveAndFlush(fdaDrug);
        when(mockFdaDrugSearchRepository.search("id:" + fdaDrug.getId(), PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(Collections.singletonList(fdaDrug), PageRequest.of(0, 1), 1));

        // Search the fdaDrug
        restFdaDrugMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + fdaDrug.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(fdaDrug.getId().intValue())))
            .andExpect(jsonPath("$.[*].applicationNumber").value(hasItem(DEFAULT_APPLICATION_NUMBER)))
            .andExpect(jsonPath("$.[*].brandName").value(hasItem(DEFAULT_BRAND_NAME)))
            .andExpect(jsonPath("$.[*].genericName").value(hasItem(DEFAULT_GENERIC_NAME)));
    }
}
