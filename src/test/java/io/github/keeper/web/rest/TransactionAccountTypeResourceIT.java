package io.github.keeper.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import io.github.keeper.IntegrationTest;
import io.github.keeper.domain.TransactionAccountType;
import io.github.keeper.repository.EntityManager;
import io.github.keeper.repository.TransactionAccountTypeRepository;
import io.github.keeper.repository.search.TransactionAccountTypeSearchRepository;
import io.github.keeper.service.dto.TransactionAccountTypeDTO;
import io.github.keeper.service.mapper.TransactionAccountTypeMapper;
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
 * Integration tests for the {@link TransactionAccountTypeResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class TransactionAccountTypeResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/transaction-account-types";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/transaction-account-types";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private TransactionAccountTypeRepository transactionAccountTypeRepository;

    @Autowired
    private TransactionAccountTypeMapper transactionAccountTypeMapper;

    @Autowired
    private TransactionAccountTypeSearchRepository transactionAccountTypeSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private TransactionAccountType transactionAccountType;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TransactionAccountType createEntity(EntityManager em) {
        TransactionAccountType transactionAccountType = new TransactionAccountType().name(DEFAULT_NAME);
        return transactionAccountType;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TransactionAccountType createUpdatedEntity(EntityManager em) {
        TransactionAccountType transactionAccountType = new TransactionAccountType().name(UPDATED_NAME);
        return transactionAccountType;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(TransactionAccountType.class).block();
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
        transactionAccountTypeSearchRepository.deleteAll().block();
        assertThat(transactionAccountTypeSearchRepository.count().block()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        transactionAccountType = createEntity(em);
    }

    @Test
    void createTransactionAccountType() throws Exception {
        int databaseSizeBeforeCreate = transactionAccountTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionAccountTypeSearchRepository.findAll().collectList().block());
        // Create the TransactionAccountType
        TransactionAccountTypeDTO transactionAccountTypeDTO = transactionAccountTypeMapper.toDto(transactionAccountType);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionAccountTypeDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the TransactionAccountType in the database
        List<TransactionAccountType> transactionAccountTypeList = transactionAccountTypeRepository.findAll().collectList().block();
        assertThat(transactionAccountTypeList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionAccountTypeSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        TransactionAccountType testTransactionAccountType = transactionAccountTypeList.get(transactionAccountTypeList.size() - 1);
        assertThat(testTransactionAccountType.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    void createTransactionAccountTypeWithExistingId() throws Exception {
        // Create the TransactionAccountType with an existing ID
        transactionAccountType.setId(1L);
        TransactionAccountTypeDTO transactionAccountTypeDTO = transactionAccountTypeMapper.toDto(transactionAccountType);

        int databaseSizeBeforeCreate = transactionAccountTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionAccountTypeSearchRepository.findAll().collectList().block());

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionAccountTypeDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TransactionAccountType in the database
        List<TransactionAccountType> transactionAccountTypeList = transactionAccountTypeRepository.findAll().collectList().block();
        assertThat(transactionAccountTypeList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionAccountTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = transactionAccountTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionAccountTypeSearchRepository.findAll().collectList().block());
        // set the field null
        transactionAccountType.setName(null);

        // Create the TransactionAccountType, which fails.
        TransactionAccountTypeDTO transactionAccountTypeDTO = transactionAccountTypeMapper.toDto(transactionAccountType);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionAccountTypeDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<TransactionAccountType> transactionAccountTypeList = transactionAccountTypeRepository.findAll().collectList().block();
        assertThat(transactionAccountTypeList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionAccountTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void getAllTransactionAccountTypes() {
        // Initialize the database
        transactionAccountTypeRepository.save(transactionAccountType).block();

        // Get all the transactionAccountTypeList
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
            .value(hasItem(transactionAccountType.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME));
    }

    @Test
    void getTransactionAccountType() {
        // Initialize the database
        transactionAccountTypeRepository.save(transactionAccountType).block();

        // Get the transactionAccountType
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, transactionAccountType.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(transactionAccountType.getId().intValue()))
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME));
    }

    @Test
    void getNonExistingTransactionAccountType() {
        // Get the transactionAccountType
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingTransactionAccountType() throws Exception {
        // Initialize the database
        transactionAccountTypeRepository.save(transactionAccountType).block();

        int databaseSizeBeforeUpdate = transactionAccountTypeRepository.findAll().collectList().block().size();
        transactionAccountTypeSearchRepository.save(transactionAccountType).block();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionAccountTypeSearchRepository.findAll().collectList().block());

        // Update the transactionAccountType
        TransactionAccountType updatedTransactionAccountType = transactionAccountTypeRepository
            .findById(transactionAccountType.getId())
            .block();
        updatedTransactionAccountType.name(UPDATED_NAME);
        TransactionAccountTypeDTO transactionAccountTypeDTO = transactionAccountTypeMapper.toDto(updatedTransactionAccountType);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, transactionAccountTypeDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionAccountTypeDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the TransactionAccountType in the database
        List<TransactionAccountType> transactionAccountTypeList = transactionAccountTypeRepository.findAll().collectList().block();
        assertThat(transactionAccountTypeList).hasSize(databaseSizeBeforeUpdate);
        TransactionAccountType testTransactionAccountType = transactionAccountTypeList.get(transactionAccountTypeList.size() - 1);
        assertThat(testTransactionAccountType.getName()).isEqualTo(UPDATED_NAME);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionAccountTypeSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<TransactionAccountType> transactionAccountTypeSearchList = IterableUtils.toList(
                    transactionAccountTypeSearchRepository.findAll().collectList().block()
                );
                TransactionAccountType testTransactionAccountTypeSearch = transactionAccountTypeSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testTransactionAccountTypeSearch.getName()).isEqualTo(UPDATED_NAME);
            });
    }

    @Test
    void putNonExistingTransactionAccountType() throws Exception {
        int databaseSizeBeforeUpdate = transactionAccountTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionAccountTypeSearchRepository.findAll().collectList().block());
        transactionAccountType.setId(count.incrementAndGet());

        // Create the TransactionAccountType
        TransactionAccountTypeDTO transactionAccountTypeDTO = transactionAccountTypeMapper.toDto(transactionAccountType);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, transactionAccountTypeDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionAccountTypeDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TransactionAccountType in the database
        List<TransactionAccountType> transactionAccountTypeList = transactionAccountTypeRepository.findAll().collectList().block();
        assertThat(transactionAccountTypeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionAccountTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithIdMismatchTransactionAccountType() throws Exception {
        int databaseSizeBeforeUpdate = transactionAccountTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionAccountTypeSearchRepository.findAll().collectList().block());
        transactionAccountType.setId(count.incrementAndGet());

        // Create the TransactionAccountType
        TransactionAccountTypeDTO transactionAccountTypeDTO = transactionAccountTypeMapper.toDto(transactionAccountType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionAccountTypeDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TransactionAccountType in the database
        List<TransactionAccountType> transactionAccountTypeList = transactionAccountTypeRepository.findAll().collectList().block();
        assertThat(transactionAccountTypeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionAccountTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithMissingIdPathParamTransactionAccountType() throws Exception {
        int databaseSizeBeforeUpdate = transactionAccountTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionAccountTypeSearchRepository.findAll().collectList().block());
        transactionAccountType.setId(count.incrementAndGet());

        // Create the TransactionAccountType
        TransactionAccountTypeDTO transactionAccountTypeDTO = transactionAccountTypeMapper.toDto(transactionAccountType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionAccountTypeDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the TransactionAccountType in the database
        List<TransactionAccountType> transactionAccountTypeList = transactionAccountTypeRepository.findAll().collectList().block();
        assertThat(transactionAccountTypeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionAccountTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void partialUpdateTransactionAccountTypeWithPatch() throws Exception {
        // Initialize the database
        transactionAccountTypeRepository.save(transactionAccountType).block();

        int databaseSizeBeforeUpdate = transactionAccountTypeRepository.findAll().collectList().block().size();

        // Update the transactionAccountType using partial update
        TransactionAccountType partialUpdatedTransactionAccountType = new TransactionAccountType();
        partialUpdatedTransactionAccountType.setId(transactionAccountType.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedTransactionAccountType.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedTransactionAccountType))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the TransactionAccountType in the database
        List<TransactionAccountType> transactionAccountTypeList = transactionAccountTypeRepository.findAll().collectList().block();
        assertThat(transactionAccountTypeList).hasSize(databaseSizeBeforeUpdate);
        TransactionAccountType testTransactionAccountType = transactionAccountTypeList.get(transactionAccountTypeList.size() - 1);
        assertThat(testTransactionAccountType.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    void fullUpdateTransactionAccountTypeWithPatch() throws Exception {
        // Initialize the database
        transactionAccountTypeRepository.save(transactionAccountType).block();

        int databaseSizeBeforeUpdate = transactionAccountTypeRepository.findAll().collectList().block().size();

        // Update the transactionAccountType using partial update
        TransactionAccountType partialUpdatedTransactionAccountType = new TransactionAccountType();
        partialUpdatedTransactionAccountType.setId(transactionAccountType.getId());

        partialUpdatedTransactionAccountType.name(UPDATED_NAME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedTransactionAccountType.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedTransactionAccountType))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the TransactionAccountType in the database
        List<TransactionAccountType> transactionAccountTypeList = transactionAccountTypeRepository.findAll().collectList().block();
        assertThat(transactionAccountTypeList).hasSize(databaseSizeBeforeUpdate);
        TransactionAccountType testTransactionAccountType = transactionAccountTypeList.get(transactionAccountTypeList.size() - 1);
        assertThat(testTransactionAccountType.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    void patchNonExistingTransactionAccountType() throws Exception {
        int databaseSizeBeforeUpdate = transactionAccountTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionAccountTypeSearchRepository.findAll().collectList().block());
        transactionAccountType.setId(count.incrementAndGet());

        // Create the TransactionAccountType
        TransactionAccountTypeDTO transactionAccountTypeDTO = transactionAccountTypeMapper.toDto(transactionAccountType);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, transactionAccountTypeDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionAccountTypeDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TransactionAccountType in the database
        List<TransactionAccountType> transactionAccountTypeList = transactionAccountTypeRepository.findAll().collectList().block();
        assertThat(transactionAccountTypeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionAccountTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithIdMismatchTransactionAccountType() throws Exception {
        int databaseSizeBeforeUpdate = transactionAccountTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionAccountTypeSearchRepository.findAll().collectList().block());
        transactionAccountType.setId(count.incrementAndGet());

        // Create the TransactionAccountType
        TransactionAccountTypeDTO transactionAccountTypeDTO = transactionAccountTypeMapper.toDto(transactionAccountType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionAccountTypeDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TransactionAccountType in the database
        List<TransactionAccountType> transactionAccountTypeList = transactionAccountTypeRepository.findAll().collectList().block();
        assertThat(transactionAccountTypeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionAccountTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithMissingIdPathParamTransactionAccountType() throws Exception {
        int databaseSizeBeforeUpdate = transactionAccountTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionAccountTypeSearchRepository.findAll().collectList().block());
        transactionAccountType.setId(count.incrementAndGet());

        // Create the TransactionAccountType
        TransactionAccountTypeDTO transactionAccountTypeDTO = transactionAccountTypeMapper.toDto(transactionAccountType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionAccountTypeDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the TransactionAccountType in the database
        List<TransactionAccountType> transactionAccountTypeList = transactionAccountTypeRepository.findAll().collectList().block();
        assertThat(transactionAccountTypeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionAccountTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void deleteTransactionAccountType() {
        // Initialize the database
        transactionAccountTypeRepository.save(transactionAccountType).block();
        transactionAccountTypeRepository.save(transactionAccountType).block();
        transactionAccountTypeSearchRepository.save(transactionAccountType).block();

        int databaseSizeBeforeDelete = transactionAccountTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionAccountTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the transactionAccountType
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, transactionAccountType.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<TransactionAccountType> transactionAccountTypeList = transactionAccountTypeRepository.findAll().collectList().block();
        assertThat(transactionAccountTypeList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionAccountTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    void searchTransactionAccountType() {
        // Initialize the database
        transactionAccountType = transactionAccountTypeRepository.save(transactionAccountType).block();
        transactionAccountTypeSearchRepository.save(transactionAccountType).block();

        // Search the transactionAccountType
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + transactionAccountType.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(transactionAccountType.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME));
    }
}
