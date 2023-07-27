package io.github.keeper.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import io.github.keeper.IntegrationTest;
import io.github.keeper.domain.TransactionCurrency;
import io.github.keeper.repository.EntityManager;
import io.github.keeper.repository.TransactionCurrencyRepository;
import io.github.keeper.repository.search.TransactionCurrencySearchRepository;
import io.github.keeper.service.dto.TransactionCurrencyDTO;
import io.github.keeper.service.mapper.TransactionCurrencyMapper;
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
 * Integration tests for the {@link TransactionCurrencyResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class TransactionCurrencyResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/transaction-currencies";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/transaction-currencies";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private TransactionCurrencyRepository transactionCurrencyRepository;

    @Autowired
    private TransactionCurrencyMapper transactionCurrencyMapper;

    @Autowired
    private TransactionCurrencySearchRepository transactionCurrencySearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private TransactionCurrency transactionCurrency;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TransactionCurrency createEntity(EntityManager em) {
        TransactionCurrency transactionCurrency = new TransactionCurrency().name(DEFAULT_NAME).code(DEFAULT_CODE);
        return transactionCurrency;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TransactionCurrency createUpdatedEntity(EntityManager em) {
        TransactionCurrency transactionCurrency = new TransactionCurrency().name(UPDATED_NAME).code(UPDATED_CODE);
        return transactionCurrency;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(TransactionCurrency.class).block();
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
        transactionCurrencySearchRepository.deleteAll().block();
        assertThat(transactionCurrencySearchRepository.count().block()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        transactionCurrency = createEntity(em);
    }

    @Test
    void createTransactionCurrency() throws Exception {
        int databaseSizeBeforeCreate = transactionCurrencyRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionCurrencySearchRepository.findAll().collectList().block());
        // Create the TransactionCurrency
        TransactionCurrencyDTO transactionCurrencyDTO = transactionCurrencyMapper.toDto(transactionCurrency);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionCurrencyDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the TransactionCurrency in the database
        List<TransactionCurrency> transactionCurrencyList = transactionCurrencyRepository.findAll().collectList().block();
        assertThat(transactionCurrencyList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionCurrencySearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        TransactionCurrency testTransactionCurrency = transactionCurrencyList.get(transactionCurrencyList.size() - 1);
        assertThat(testTransactionCurrency.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testTransactionCurrency.getCode()).isEqualTo(DEFAULT_CODE);
    }

    @Test
    void createTransactionCurrencyWithExistingId() throws Exception {
        // Create the TransactionCurrency with an existing ID
        transactionCurrency.setId(1L);
        TransactionCurrencyDTO transactionCurrencyDTO = transactionCurrencyMapper.toDto(transactionCurrency);

        int databaseSizeBeforeCreate = transactionCurrencyRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionCurrencySearchRepository.findAll().collectList().block());

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionCurrencyDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TransactionCurrency in the database
        List<TransactionCurrency> transactionCurrencyList = transactionCurrencyRepository.findAll().collectList().block();
        assertThat(transactionCurrencyList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionCurrencySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = transactionCurrencyRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionCurrencySearchRepository.findAll().collectList().block());
        // set the field null
        transactionCurrency.setName(null);

        // Create the TransactionCurrency, which fails.
        TransactionCurrencyDTO transactionCurrencyDTO = transactionCurrencyMapper.toDto(transactionCurrency);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionCurrencyDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<TransactionCurrency> transactionCurrencyList = transactionCurrencyRepository.findAll().collectList().block();
        assertThat(transactionCurrencyList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionCurrencySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkCodeIsRequired() throws Exception {
        int databaseSizeBeforeTest = transactionCurrencyRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionCurrencySearchRepository.findAll().collectList().block());
        // set the field null
        transactionCurrency.setCode(null);

        // Create the TransactionCurrency, which fails.
        TransactionCurrencyDTO transactionCurrencyDTO = transactionCurrencyMapper.toDto(transactionCurrency);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionCurrencyDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<TransactionCurrency> transactionCurrencyList = transactionCurrencyRepository.findAll().collectList().block();
        assertThat(transactionCurrencyList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionCurrencySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void getAllTransactionCurrencies() {
        // Initialize the database
        transactionCurrencyRepository.save(transactionCurrency).block();

        // Get all the transactionCurrencyList
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
            .value(hasItem(transactionCurrency.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].code")
            .value(hasItem(DEFAULT_CODE));
    }

    @Test
    void getTransactionCurrency() {
        // Initialize the database
        transactionCurrencyRepository.save(transactionCurrency).block();

        // Get the transactionCurrency
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, transactionCurrency.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(transactionCurrency.getId().intValue()))
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME))
            .jsonPath("$.code")
            .value(is(DEFAULT_CODE));
    }

    @Test
    void getNonExistingTransactionCurrency() {
        // Get the transactionCurrency
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingTransactionCurrency() throws Exception {
        // Initialize the database
        transactionCurrencyRepository.save(transactionCurrency).block();

        int databaseSizeBeforeUpdate = transactionCurrencyRepository.findAll().collectList().block().size();
        transactionCurrencySearchRepository.save(transactionCurrency).block();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionCurrencySearchRepository.findAll().collectList().block());

        // Update the transactionCurrency
        TransactionCurrency updatedTransactionCurrency = transactionCurrencyRepository.findById(transactionCurrency.getId()).block();
        updatedTransactionCurrency.name(UPDATED_NAME).code(UPDATED_CODE);
        TransactionCurrencyDTO transactionCurrencyDTO = transactionCurrencyMapper.toDto(updatedTransactionCurrency);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, transactionCurrencyDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionCurrencyDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the TransactionCurrency in the database
        List<TransactionCurrency> transactionCurrencyList = transactionCurrencyRepository.findAll().collectList().block();
        assertThat(transactionCurrencyList).hasSize(databaseSizeBeforeUpdate);
        TransactionCurrency testTransactionCurrency = transactionCurrencyList.get(transactionCurrencyList.size() - 1);
        assertThat(testTransactionCurrency.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testTransactionCurrency.getCode()).isEqualTo(UPDATED_CODE);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionCurrencySearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<TransactionCurrency> transactionCurrencySearchList = IterableUtils.toList(
                    transactionCurrencySearchRepository.findAll().collectList().block()
                );
                TransactionCurrency testTransactionCurrencySearch = transactionCurrencySearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testTransactionCurrencySearch.getName()).isEqualTo(UPDATED_NAME);
                assertThat(testTransactionCurrencySearch.getCode()).isEqualTo(UPDATED_CODE);
            });
    }

    @Test
    void putNonExistingTransactionCurrency() throws Exception {
        int databaseSizeBeforeUpdate = transactionCurrencyRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionCurrencySearchRepository.findAll().collectList().block());
        transactionCurrency.setId(count.incrementAndGet());

        // Create the TransactionCurrency
        TransactionCurrencyDTO transactionCurrencyDTO = transactionCurrencyMapper.toDto(transactionCurrency);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, transactionCurrencyDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionCurrencyDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TransactionCurrency in the database
        List<TransactionCurrency> transactionCurrencyList = transactionCurrencyRepository.findAll().collectList().block();
        assertThat(transactionCurrencyList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionCurrencySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithIdMismatchTransactionCurrency() throws Exception {
        int databaseSizeBeforeUpdate = transactionCurrencyRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionCurrencySearchRepository.findAll().collectList().block());
        transactionCurrency.setId(count.incrementAndGet());

        // Create the TransactionCurrency
        TransactionCurrencyDTO transactionCurrencyDTO = transactionCurrencyMapper.toDto(transactionCurrency);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionCurrencyDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TransactionCurrency in the database
        List<TransactionCurrency> transactionCurrencyList = transactionCurrencyRepository.findAll().collectList().block();
        assertThat(transactionCurrencyList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionCurrencySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithMissingIdPathParamTransactionCurrency() throws Exception {
        int databaseSizeBeforeUpdate = transactionCurrencyRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionCurrencySearchRepository.findAll().collectList().block());
        transactionCurrency.setId(count.incrementAndGet());

        // Create the TransactionCurrency
        TransactionCurrencyDTO transactionCurrencyDTO = transactionCurrencyMapper.toDto(transactionCurrency);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionCurrencyDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the TransactionCurrency in the database
        List<TransactionCurrency> transactionCurrencyList = transactionCurrencyRepository.findAll().collectList().block();
        assertThat(transactionCurrencyList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionCurrencySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void partialUpdateTransactionCurrencyWithPatch() throws Exception {
        // Initialize the database
        transactionCurrencyRepository.save(transactionCurrency).block();

        int databaseSizeBeforeUpdate = transactionCurrencyRepository.findAll().collectList().block().size();

        // Update the transactionCurrency using partial update
        TransactionCurrency partialUpdatedTransactionCurrency = new TransactionCurrency();
        partialUpdatedTransactionCurrency.setId(transactionCurrency.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedTransactionCurrency.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedTransactionCurrency))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the TransactionCurrency in the database
        List<TransactionCurrency> transactionCurrencyList = transactionCurrencyRepository.findAll().collectList().block();
        assertThat(transactionCurrencyList).hasSize(databaseSizeBeforeUpdate);
        TransactionCurrency testTransactionCurrency = transactionCurrencyList.get(transactionCurrencyList.size() - 1);
        assertThat(testTransactionCurrency.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testTransactionCurrency.getCode()).isEqualTo(DEFAULT_CODE);
    }

    @Test
    void fullUpdateTransactionCurrencyWithPatch() throws Exception {
        // Initialize the database
        transactionCurrencyRepository.save(transactionCurrency).block();

        int databaseSizeBeforeUpdate = transactionCurrencyRepository.findAll().collectList().block().size();

        // Update the transactionCurrency using partial update
        TransactionCurrency partialUpdatedTransactionCurrency = new TransactionCurrency();
        partialUpdatedTransactionCurrency.setId(transactionCurrency.getId());

        partialUpdatedTransactionCurrency.name(UPDATED_NAME).code(UPDATED_CODE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedTransactionCurrency.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedTransactionCurrency))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the TransactionCurrency in the database
        List<TransactionCurrency> transactionCurrencyList = transactionCurrencyRepository.findAll().collectList().block();
        assertThat(transactionCurrencyList).hasSize(databaseSizeBeforeUpdate);
        TransactionCurrency testTransactionCurrency = transactionCurrencyList.get(transactionCurrencyList.size() - 1);
        assertThat(testTransactionCurrency.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testTransactionCurrency.getCode()).isEqualTo(UPDATED_CODE);
    }

    @Test
    void patchNonExistingTransactionCurrency() throws Exception {
        int databaseSizeBeforeUpdate = transactionCurrencyRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionCurrencySearchRepository.findAll().collectList().block());
        transactionCurrency.setId(count.incrementAndGet());

        // Create the TransactionCurrency
        TransactionCurrencyDTO transactionCurrencyDTO = transactionCurrencyMapper.toDto(transactionCurrency);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, transactionCurrencyDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionCurrencyDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TransactionCurrency in the database
        List<TransactionCurrency> transactionCurrencyList = transactionCurrencyRepository.findAll().collectList().block();
        assertThat(transactionCurrencyList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionCurrencySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithIdMismatchTransactionCurrency() throws Exception {
        int databaseSizeBeforeUpdate = transactionCurrencyRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionCurrencySearchRepository.findAll().collectList().block());
        transactionCurrency.setId(count.incrementAndGet());

        // Create the TransactionCurrency
        TransactionCurrencyDTO transactionCurrencyDTO = transactionCurrencyMapper.toDto(transactionCurrency);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionCurrencyDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TransactionCurrency in the database
        List<TransactionCurrency> transactionCurrencyList = transactionCurrencyRepository.findAll().collectList().block();
        assertThat(transactionCurrencyList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionCurrencySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithMissingIdPathParamTransactionCurrency() throws Exception {
        int databaseSizeBeforeUpdate = transactionCurrencyRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionCurrencySearchRepository.findAll().collectList().block());
        transactionCurrency.setId(count.incrementAndGet());

        // Create the TransactionCurrency
        TransactionCurrencyDTO transactionCurrencyDTO = transactionCurrencyMapper.toDto(transactionCurrency);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionCurrencyDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the TransactionCurrency in the database
        List<TransactionCurrency> transactionCurrencyList = transactionCurrencyRepository.findAll().collectList().block();
        assertThat(transactionCurrencyList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionCurrencySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void deleteTransactionCurrency() {
        // Initialize the database
        transactionCurrencyRepository.save(transactionCurrency).block();
        transactionCurrencyRepository.save(transactionCurrency).block();
        transactionCurrencySearchRepository.save(transactionCurrency).block();

        int databaseSizeBeforeDelete = transactionCurrencyRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionCurrencySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the transactionCurrency
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, transactionCurrency.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<TransactionCurrency> transactionCurrencyList = transactionCurrencyRepository.findAll().collectList().block();
        assertThat(transactionCurrencyList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionCurrencySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    void searchTransactionCurrency() {
        // Initialize the database
        transactionCurrency = transactionCurrencyRepository.save(transactionCurrency).block();
        transactionCurrencySearchRepository.save(transactionCurrency).block();

        // Search the transactionCurrency
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + transactionCurrency.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(transactionCurrency.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].code")
            .value(hasItem(DEFAULT_CODE));
    }
}
