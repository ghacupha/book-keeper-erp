package io.github.keeper.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import io.github.keeper.IntegrationTest;
import io.github.keeper.domain.DealerType;
import io.github.keeper.repository.DealerTypeRepository;
import io.github.keeper.repository.EntityManager;
import io.github.keeper.repository.search.DealerTypeSearchRepository;
import io.github.keeper.service.dto.DealerTypeDTO;
import io.github.keeper.service.mapper.DealerTypeMapper;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.collections4.IterableUtils;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Integration tests for the {@link DealerTypeResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class DealerTypeResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/dealer-types";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/dealer-types";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private DealerTypeRepository dealerTypeRepository;

    @Autowired
    private DealerTypeMapper dealerTypeMapper;

    @Autowired
    private DealerTypeSearchRepository dealerTypeSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private DealerType dealerType;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DealerType createEntity(EntityManager em) {
        DealerType dealerType = new DealerType().name(DEFAULT_NAME);
        return dealerType;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DealerType createUpdatedEntity(EntityManager em) {
        DealerType dealerType = new DealerType().name(UPDATED_NAME);
        return dealerType;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(DealerType.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @AfterEach
    public void cleanup() {
        deleteEntities(em);
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        dealerTypeSearchRepository.deleteAll().block();
        assertThat(dealerTypeSearchRepository.count().block()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        dealerType = createEntity(em);
    }

    @Test
    void createDealerType() throws Exception {
        int databaseSizeBeforeCreate = dealerTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dealerTypeSearchRepository.findAll().collectList().block());
        // Create the DealerType
        DealerTypeDTO dealerTypeDTO = dealerTypeMapper.toDto(dealerType);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(dealerTypeDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the DealerType in the database
        List<DealerType> dealerTypeList = dealerTypeRepository.findAll().collectList().block();
        assertThat(dealerTypeList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(dealerTypeSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        DealerType testDealerType = dealerTypeList.get(dealerTypeList.size() - 1);
        assertThat(testDealerType.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    void createDealerTypeWithExistingId() throws Exception {
        // Create the DealerType with an existing ID
        dealerType.setId(1L);
        DealerTypeDTO dealerTypeDTO = dealerTypeMapper.toDto(dealerType);

        int databaseSizeBeforeCreate = dealerTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dealerTypeSearchRepository.findAll().collectList().block());

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(dealerTypeDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the DealerType in the database
        List<DealerType> dealerTypeList = dealerTypeRepository.findAll().collectList().block();
        assertThat(dealerTypeList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(dealerTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = dealerTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dealerTypeSearchRepository.findAll().collectList().block());
        // set the field null
        dealerType.setName(null);

        // Create the DealerType, which fails.
        DealerTypeDTO dealerTypeDTO = dealerTypeMapper.toDto(dealerType);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(dealerTypeDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<DealerType> dealerTypeList = dealerTypeRepository.findAll().collectList().block();
        assertThat(dealerTypeList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(dealerTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void getAllDealerTypes() {
        // Initialize the database
        dealerTypeRepository.save(dealerType).block();

        // Get all the dealerTypeList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(dealerType.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME));
    }

    @Test
    void getDealerType() {
        // Initialize the database
        dealerTypeRepository.save(dealerType).block();

        // Get the dealerType
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, dealerType.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(dealerType.getId().intValue()))
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME));
    }

    @Test
    void getNonExistingDealerType() {
        // Get the dealerType
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingDealerType() throws Exception {
        // Initialize the database
        dealerTypeRepository.save(dealerType).block();

        int databaseSizeBeforeUpdate = dealerTypeRepository.findAll().collectList().block().size();
        dealerTypeSearchRepository.save(dealerType).block();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dealerTypeSearchRepository.findAll().collectList().block());

        // Update the dealerType
        DealerType updatedDealerType = dealerTypeRepository.findById(dealerType.getId()).block();
        updatedDealerType.name(UPDATED_NAME);
        DealerTypeDTO dealerTypeDTO = dealerTypeMapper.toDto(updatedDealerType);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, dealerTypeDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(dealerTypeDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the DealerType in the database
        List<DealerType> dealerTypeList = dealerTypeRepository.findAll().collectList().block();
        assertThat(dealerTypeList).hasSize(databaseSizeBeforeUpdate);
        DealerType testDealerType = dealerTypeList.get(dealerTypeList.size() - 1);
        assertThat(testDealerType.getName()).isEqualTo(UPDATED_NAME);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(dealerTypeSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<DealerType> dealerTypeSearchList = IterableUtils.toList(dealerTypeSearchRepository.findAll().collectList().block());
                DealerType testDealerTypeSearch = dealerTypeSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testDealerTypeSearch.getName()).isEqualTo(UPDATED_NAME);
            });
    }

    @Test
    void putNonExistingDealerType() throws Exception {
        int databaseSizeBeforeUpdate = dealerTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dealerTypeSearchRepository.findAll().collectList().block());
        dealerType.setId(count.incrementAndGet());

        // Create the DealerType
        DealerTypeDTO dealerTypeDTO = dealerTypeMapper.toDto(dealerType);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, dealerTypeDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(dealerTypeDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the DealerType in the database
        List<DealerType> dealerTypeList = dealerTypeRepository.findAll().collectList().block();
        assertThat(dealerTypeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(dealerTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithIdMismatchDealerType() throws Exception {
        int databaseSizeBeforeUpdate = dealerTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dealerTypeSearchRepository.findAll().collectList().block());
        dealerType.setId(count.incrementAndGet());

        // Create the DealerType
        DealerTypeDTO dealerTypeDTO = dealerTypeMapper.toDto(dealerType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(dealerTypeDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the DealerType in the database
        List<DealerType> dealerTypeList = dealerTypeRepository.findAll().collectList().block();
        assertThat(dealerTypeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(dealerTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithMissingIdPathParamDealerType() throws Exception {
        int databaseSizeBeforeUpdate = dealerTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dealerTypeSearchRepository.findAll().collectList().block());
        dealerType.setId(count.incrementAndGet());

        // Create the DealerType
        DealerTypeDTO dealerTypeDTO = dealerTypeMapper.toDto(dealerType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(dealerTypeDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the DealerType in the database
        List<DealerType> dealerTypeList = dealerTypeRepository.findAll().collectList().block();
        assertThat(dealerTypeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(dealerTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void partialUpdateDealerTypeWithPatch() throws Exception {
        // Initialize the database
        dealerTypeRepository.save(dealerType).block();

        int databaseSizeBeforeUpdate = dealerTypeRepository.findAll().collectList().block().size();

        // Update the dealerType using partial update
        DealerType partialUpdatedDealerType = new DealerType();
        partialUpdatedDealerType.setId(dealerType.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedDealerType.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedDealerType))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the DealerType in the database
        List<DealerType> dealerTypeList = dealerTypeRepository.findAll().collectList().block();
        assertThat(dealerTypeList).hasSize(databaseSizeBeforeUpdate);
        DealerType testDealerType = dealerTypeList.get(dealerTypeList.size() - 1);
        assertThat(testDealerType.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    void fullUpdateDealerTypeWithPatch() throws Exception {
        // Initialize the database
        dealerTypeRepository.save(dealerType).block();

        int databaseSizeBeforeUpdate = dealerTypeRepository.findAll().collectList().block().size();

        // Update the dealerType using partial update
        DealerType partialUpdatedDealerType = new DealerType();
        partialUpdatedDealerType.setId(dealerType.getId());

        partialUpdatedDealerType.name(UPDATED_NAME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedDealerType.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedDealerType))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the DealerType in the database
        List<DealerType> dealerTypeList = dealerTypeRepository.findAll().collectList().block();
        assertThat(dealerTypeList).hasSize(databaseSizeBeforeUpdate);
        DealerType testDealerType = dealerTypeList.get(dealerTypeList.size() - 1);
        assertThat(testDealerType.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    void patchNonExistingDealerType() throws Exception {
        int databaseSizeBeforeUpdate = dealerTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dealerTypeSearchRepository.findAll().collectList().block());
        dealerType.setId(count.incrementAndGet());

        // Create the DealerType
        DealerTypeDTO dealerTypeDTO = dealerTypeMapper.toDto(dealerType);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, dealerTypeDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(dealerTypeDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the DealerType in the database
        List<DealerType> dealerTypeList = dealerTypeRepository.findAll().collectList().block();
        assertThat(dealerTypeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(dealerTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithIdMismatchDealerType() throws Exception {
        int databaseSizeBeforeUpdate = dealerTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dealerTypeSearchRepository.findAll().collectList().block());
        dealerType.setId(count.incrementAndGet());

        // Create the DealerType
        DealerTypeDTO dealerTypeDTO = dealerTypeMapper.toDto(dealerType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(dealerTypeDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the DealerType in the database
        List<DealerType> dealerTypeList = dealerTypeRepository.findAll().collectList().block();
        assertThat(dealerTypeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(dealerTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithMissingIdPathParamDealerType() throws Exception {
        int databaseSizeBeforeUpdate = dealerTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dealerTypeSearchRepository.findAll().collectList().block());
        dealerType.setId(count.incrementAndGet());

        // Create the DealerType
        DealerTypeDTO dealerTypeDTO = dealerTypeMapper.toDto(dealerType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(dealerTypeDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the DealerType in the database
        List<DealerType> dealerTypeList = dealerTypeRepository.findAll().collectList().block();
        assertThat(dealerTypeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(dealerTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void deleteDealerType() {
        // Initialize the database
        dealerTypeRepository.save(dealerType).block();
        dealerTypeRepository.save(dealerType).block();
        dealerTypeSearchRepository.save(dealerType).block();

        int databaseSizeBeforeDelete = dealerTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dealerTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the dealerType
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, dealerType.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<DealerType> dealerTypeList = dealerTypeRepository.findAll().collectList().block();
        assertThat(dealerTypeList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(dealerTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    void searchDealerType() {
        // Initialize the database
        dealerType = dealerTypeRepository.save(dealerType).block();
        dealerTypeSearchRepository.save(dealerType).block();

        // Search the dealerType
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + dealerType.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(dealerType.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME));
    }
}
