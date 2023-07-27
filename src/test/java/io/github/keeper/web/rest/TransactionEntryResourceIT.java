package io.github.keeper.web.rest;

import static io.github.keeper.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import io.github.keeper.IntegrationTest;
import io.github.keeper.domain.TransactionAccount;
import io.github.keeper.domain.TransactionEntry;
import io.github.keeper.domain.enumeration.TransactionEntryTypes;
import io.github.keeper.repository.EntityManager;
import io.github.keeper.repository.TransactionEntryRepository;
import io.github.keeper.repository.search.TransactionEntrySearchRepository;
import io.github.keeper.service.TransactionEntryService;
import io.github.keeper.service.dto.TransactionEntryDTO;
import io.github.keeper.service.mapper.TransactionEntryMapper;
import java.math.BigDecimal;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
 * Integration tests for the {@link TransactionEntryResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class TransactionEntryResourceIT {

    private static final BigDecimal DEFAULT_ENTRY_AMOUNT = new BigDecimal(1);
    private static final BigDecimal UPDATED_ENTRY_AMOUNT = new BigDecimal(2);

    private static final TransactionEntryTypes DEFAULT_TRANSACTION_ENTRY_TYPE = TransactionEntryTypes.DEBIT;
    private static final TransactionEntryTypes UPDATED_TRANSACTION_ENTRY_TYPE = TransactionEntryTypes.CREDIT;

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final Boolean DEFAULT_WAS_PROPOSED = false;
    private static final Boolean UPDATED_WAS_PROPOSED = true;

    private static final Boolean DEFAULT_WAS_POSTED = false;
    private static final Boolean UPDATED_WAS_POSTED = true;

    private static final Boolean DEFAULT_WAS_DELETED = false;
    private static final Boolean UPDATED_WAS_DELETED = true;

    private static final Boolean DEFAULT_WAS_APPROVED = false;
    private static final Boolean UPDATED_WAS_APPROVED = true;

    private static final String ENTITY_API_URL = "/api/transaction-entries";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/transaction-entries";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private TransactionEntryRepository transactionEntryRepository;

    @Mock
    private TransactionEntryRepository transactionEntryRepositoryMock;

    @Autowired
    private TransactionEntryMapper transactionEntryMapper;

    @Mock
    private TransactionEntryService transactionEntryServiceMock;

    @Autowired
    private TransactionEntrySearchRepository transactionEntrySearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private TransactionEntry transactionEntry;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TransactionEntry createEntity(EntityManager em) {
        TransactionEntry transactionEntry = new TransactionEntry()
            .entryAmount(DEFAULT_ENTRY_AMOUNT)
            .transactionEntryType(DEFAULT_TRANSACTION_ENTRY_TYPE)
            .description(DEFAULT_DESCRIPTION)
            .wasProposed(DEFAULT_WAS_PROPOSED)
            .wasPosted(DEFAULT_WAS_POSTED)
            .wasDeleted(DEFAULT_WAS_DELETED)
            .wasApproved(DEFAULT_WAS_APPROVED);
        // Add required entity
        TransactionAccount transactionAccount;
        transactionAccount = em.insert(TransactionAccountResourceIT.createEntity(em)).block();
        transactionEntry.setTransactionAccount(transactionAccount);
        return transactionEntry;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TransactionEntry createUpdatedEntity(EntityManager em) {
        TransactionEntry transactionEntry = new TransactionEntry()
            .entryAmount(UPDATED_ENTRY_AMOUNT)
            .transactionEntryType(UPDATED_TRANSACTION_ENTRY_TYPE)
            .description(UPDATED_DESCRIPTION)
            .wasProposed(UPDATED_WAS_PROPOSED)
            .wasPosted(UPDATED_WAS_POSTED)
            .wasDeleted(UPDATED_WAS_DELETED)
            .wasApproved(UPDATED_WAS_APPROVED);
        // Add required entity
        TransactionAccount transactionAccount;
        transactionAccount = em.insert(TransactionAccountResourceIT.createUpdatedEntity(em)).block();
        transactionEntry.setTransactionAccount(transactionAccount);
        return transactionEntry;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(TransactionEntry.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
        TransactionAccountResourceIT.deleteEntities(em);
    }

    @AfterEach
    public void cleanup() {
        deleteEntities(em);
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        transactionEntrySearchRepository.deleteAll().block();
        assertThat(transactionEntrySearchRepository.count().block()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        transactionEntry = createEntity(em);
    }

    @Test
    void createTransactionEntry() throws Exception {
        int databaseSizeBeforeCreate = transactionEntryRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionEntrySearchRepository.findAll().collectList().block());
        // Create the TransactionEntry
        TransactionEntryDTO transactionEntryDTO = transactionEntryMapper.toDto(transactionEntry);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionEntryDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the TransactionEntry in the database
        List<TransactionEntry> transactionEntryList = transactionEntryRepository.findAll().collectList().block();
        assertThat(transactionEntryList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionEntrySearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        TransactionEntry testTransactionEntry = transactionEntryList.get(transactionEntryList.size() - 1);
        assertThat(testTransactionEntry.getEntryAmount()).isEqualByComparingTo(DEFAULT_ENTRY_AMOUNT);
        assertThat(testTransactionEntry.getTransactionEntryType()).isEqualTo(DEFAULT_TRANSACTION_ENTRY_TYPE);
        assertThat(testTransactionEntry.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testTransactionEntry.getWasProposed()).isEqualTo(DEFAULT_WAS_PROPOSED);
        assertThat(testTransactionEntry.getWasPosted()).isEqualTo(DEFAULT_WAS_POSTED);
        assertThat(testTransactionEntry.getWasDeleted()).isEqualTo(DEFAULT_WAS_DELETED);
        assertThat(testTransactionEntry.getWasApproved()).isEqualTo(DEFAULT_WAS_APPROVED);
    }

    @Test
    void createTransactionEntryWithExistingId() throws Exception {
        // Create the TransactionEntry with an existing ID
        transactionEntry.setId(1L);
        TransactionEntryDTO transactionEntryDTO = transactionEntryMapper.toDto(transactionEntry);

        int databaseSizeBeforeCreate = transactionEntryRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionEntrySearchRepository.findAll().collectList().block());

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionEntryDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TransactionEntry in the database
        List<TransactionEntry> transactionEntryList = transactionEntryRepository.findAll().collectList().block();
        assertThat(transactionEntryList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionEntrySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkTransactionEntryTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = transactionEntryRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionEntrySearchRepository.findAll().collectList().block());
        // set the field null
        transactionEntry.setTransactionEntryType(null);

        // Create the TransactionEntry, which fails.
        TransactionEntryDTO transactionEntryDTO = transactionEntryMapper.toDto(transactionEntry);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionEntryDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<TransactionEntry> transactionEntryList = transactionEntryRepository.findAll().collectList().block();
        assertThat(transactionEntryList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionEntrySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void getAllTransactionEntries() {
        // Initialize the database
        transactionEntryRepository.save(transactionEntry).block();

        // Get all the transactionEntryList
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
            .value(hasItem(transactionEntry.getId().intValue()))
            .jsonPath("$.[*].entryAmount")
            .value(hasItem(sameNumber(DEFAULT_ENTRY_AMOUNT)))
            .jsonPath("$.[*].transactionEntryType")
            .value(hasItem(DEFAULT_TRANSACTION_ENTRY_TYPE.toString()))
            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION))
            .jsonPath("$.[*].wasProposed")
            .value(hasItem(DEFAULT_WAS_PROPOSED.booleanValue()))
            .jsonPath("$.[*].wasPosted")
            .value(hasItem(DEFAULT_WAS_POSTED.booleanValue()))
            .jsonPath("$.[*].wasDeleted")
            .value(hasItem(DEFAULT_WAS_DELETED.booleanValue()))
            .jsonPath("$.[*].wasApproved")
            .value(hasItem(DEFAULT_WAS_APPROVED.booleanValue()));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllTransactionEntriesWithEagerRelationshipsIsEnabled() {
        when(transactionEntryServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(transactionEntryServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllTransactionEntriesWithEagerRelationshipsIsNotEnabled() {
        when(transactionEntryServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=false").exchange().expectStatus().isOk();
        verify(transactionEntryRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    void getTransactionEntry() {
        // Initialize the database
        transactionEntryRepository.save(transactionEntry).block();

        // Get the transactionEntry
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, transactionEntry.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(transactionEntry.getId().intValue()))
            .jsonPath("$.entryAmount")
            .value(is(sameNumber(DEFAULT_ENTRY_AMOUNT)))
            .jsonPath("$.transactionEntryType")
            .value(is(DEFAULT_TRANSACTION_ENTRY_TYPE.toString()))
            .jsonPath("$.description")
            .value(is(DEFAULT_DESCRIPTION))
            .jsonPath("$.wasProposed")
            .value(is(DEFAULT_WAS_PROPOSED.booleanValue()))
            .jsonPath("$.wasPosted")
            .value(is(DEFAULT_WAS_POSTED.booleanValue()))
            .jsonPath("$.wasDeleted")
            .value(is(DEFAULT_WAS_DELETED.booleanValue()))
            .jsonPath("$.wasApproved")
            .value(is(DEFAULT_WAS_APPROVED.booleanValue()));
    }

    @Test
    void getNonExistingTransactionEntry() {
        // Get the transactionEntry
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingTransactionEntry() throws Exception {
        // Initialize the database
        transactionEntryRepository.save(transactionEntry).block();

        int databaseSizeBeforeUpdate = transactionEntryRepository.findAll().collectList().block().size();
        transactionEntrySearchRepository.save(transactionEntry).block();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionEntrySearchRepository.findAll().collectList().block());

        // Update the transactionEntry
        TransactionEntry updatedTransactionEntry = transactionEntryRepository.findById(transactionEntry.getId()).block();
        updatedTransactionEntry
            .entryAmount(UPDATED_ENTRY_AMOUNT)
            .transactionEntryType(UPDATED_TRANSACTION_ENTRY_TYPE)
            .description(UPDATED_DESCRIPTION)
            .wasProposed(UPDATED_WAS_PROPOSED)
            .wasPosted(UPDATED_WAS_POSTED)
            .wasDeleted(UPDATED_WAS_DELETED)
            .wasApproved(UPDATED_WAS_APPROVED);
        TransactionEntryDTO transactionEntryDTO = transactionEntryMapper.toDto(updatedTransactionEntry);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, transactionEntryDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionEntryDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the TransactionEntry in the database
        List<TransactionEntry> transactionEntryList = transactionEntryRepository.findAll().collectList().block();
        assertThat(transactionEntryList).hasSize(databaseSizeBeforeUpdate);
        TransactionEntry testTransactionEntry = transactionEntryList.get(transactionEntryList.size() - 1);
        assertThat(testTransactionEntry.getEntryAmount()).isEqualByComparingTo(UPDATED_ENTRY_AMOUNT);
        assertThat(testTransactionEntry.getTransactionEntryType()).isEqualTo(UPDATED_TRANSACTION_ENTRY_TYPE);
        assertThat(testTransactionEntry.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testTransactionEntry.getWasProposed()).isEqualTo(UPDATED_WAS_PROPOSED);
        assertThat(testTransactionEntry.getWasPosted()).isEqualTo(UPDATED_WAS_POSTED);
        assertThat(testTransactionEntry.getWasDeleted()).isEqualTo(UPDATED_WAS_DELETED);
        assertThat(testTransactionEntry.getWasApproved()).isEqualTo(UPDATED_WAS_APPROVED);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionEntrySearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<TransactionEntry> transactionEntrySearchList = IterableUtils.toList(
                    transactionEntrySearchRepository.findAll().collectList().block()
                );
                TransactionEntry testTransactionEntrySearch = transactionEntrySearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testTransactionEntrySearch.getEntryAmount()).isEqualByComparingTo(UPDATED_ENTRY_AMOUNT);
                assertThat(testTransactionEntrySearch.getTransactionEntryType()).isEqualTo(UPDATED_TRANSACTION_ENTRY_TYPE);
                assertThat(testTransactionEntrySearch.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
                assertThat(testTransactionEntrySearch.getWasProposed()).isEqualTo(UPDATED_WAS_PROPOSED);
                assertThat(testTransactionEntrySearch.getWasPosted()).isEqualTo(UPDATED_WAS_POSTED);
                assertThat(testTransactionEntrySearch.getWasDeleted()).isEqualTo(UPDATED_WAS_DELETED);
                assertThat(testTransactionEntrySearch.getWasApproved()).isEqualTo(UPDATED_WAS_APPROVED);
            });
    }

    @Test
    void putNonExistingTransactionEntry() throws Exception {
        int databaseSizeBeforeUpdate = transactionEntryRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionEntrySearchRepository.findAll().collectList().block());
        transactionEntry.setId(count.incrementAndGet());

        // Create the TransactionEntry
        TransactionEntryDTO transactionEntryDTO = transactionEntryMapper.toDto(transactionEntry);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, transactionEntryDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionEntryDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TransactionEntry in the database
        List<TransactionEntry> transactionEntryList = transactionEntryRepository.findAll().collectList().block();
        assertThat(transactionEntryList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionEntrySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithIdMismatchTransactionEntry() throws Exception {
        int databaseSizeBeforeUpdate = transactionEntryRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionEntrySearchRepository.findAll().collectList().block());
        transactionEntry.setId(count.incrementAndGet());

        // Create the TransactionEntry
        TransactionEntryDTO transactionEntryDTO = transactionEntryMapper.toDto(transactionEntry);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionEntryDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TransactionEntry in the database
        List<TransactionEntry> transactionEntryList = transactionEntryRepository.findAll().collectList().block();
        assertThat(transactionEntryList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionEntrySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithMissingIdPathParamTransactionEntry() throws Exception {
        int databaseSizeBeforeUpdate = transactionEntryRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionEntrySearchRepository.findAll().collectList().block());
        transactionEntry.setId(count.incrementAndGet());

        // Create the TransactionEntry
        TransactionEntryDTO transactionEntryDTO = transactionEntryMapper.toDto(transactionEntry);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionEntryDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the TransactionEntry in the database
        List<TransactionEntry> transactionEntryList = transactionEntryRepository.findAll().collectList().block();
        assertThat(transactionEntryList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionEntrySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void partialUpdateTransactionEntryWithPatch() throws Exception {
        // Initialize the database
        transactionEntryRepository.save(transactionEntry).block();

        int databaseSizeBeforeUpdate = transactionEntryRepository.findAll().collectList().block().size();

        // Update the transactionEntry using partial update
        TransactionEntry partialUpdatedTransactionEntry = new TransactionEntry();
        partialUpdatedTransactionEntry.setId(transactionEntry.getId());

        partialUpdatedTransactionEntry
            .entryAmount(UPDATED_ENTRY_AMOUNT)
            .wasProposed(UPDATED_WAS_PROPOSED)
            .wasPosted(UPDATED_WAS_POSTED)
            .wasApproved(UPDATED_WAS_APPROVED);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedTransactionEntry.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedTransactionEntry))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the TransactionEntry in the database
        List<TransactionEntry> transactionEntryList = transactionEntryRepository.findAll().collectList().block();
        assertThat(transactionEntryList).hasSize(databaseSizeBeforeUpdate);
        TransactionEntry testTransactionEntry = transactionEntryList.get(transactionEntryList.size() - 1);
        assertThat(testTransactionEntry.getEntryAmount()).isEqualByComparingTo(UPDATED_ENTRY_AMOUNT);
        assertThat(testTransactionEntry.getTransactionEntryType()).isEqualTo(DEFAULT_TRANSACTION_ENTRY_TYPE);
        assertThat(testTransactionEntry.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testTransactionEntry.getWasProposed()).isEqualTo(UPDATED_WAS_PROPOSED);
        assertThat(testTransactionEntry.getWasPosted()).isEqualTo(UPDATED_WAS_POSTED);
        assertThat(testTransactionEntry.getWasDeleted()).isEqualTo(DEFAULT_WAS_DELETED);
        assertThat(testTransactionEntry.getWasApproved()).isEqualTo(UPDATED_WAS_APPROVED);
    }

    @Test
    void fullUpdateTransactionEntryWithPatch() throws Exception {
        // Initialize the database
        transactionEntryRepository.save(transactionEntry).block();

        int databaseSizeBeforeUpdate = transactionEntryRepository.findAll().collectList().block().size();

        // Update the transactionEntry using partial update
        TransactionEntry partialUpdatedTransactionEntry = new TransactionEntry();
        partialUpdatedTransactionEntry.setId(transactionEntry.getId());

        partialUpdatedTransactionEntry
            .entryAmount(UPDATED_ENTRY_AMOUNT)
            .transactionEntryType(UPDATED_TRANSACTION_ENTRY_TYPE)
            .description(UPDATED_DESCRIPTION)
            .wasProposed(UPDATED_WAS_PROPOSED)
            .wasPosted(UPDATED_WAS_POSTED)
            .wasDeleted(UPDATED_WAS_DELETED)
            .wasApproved(UPDATED_WAS_APPROVED);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedTransactionEntry.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedTransactionEntry))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the TransactionEntry in the database
        List<TransactionEntry> transactionEntryList = transactionEntryRepository.findAll().collectList().block();
        assertThat(transactionEntryList).hasSize(databaseSizeBeforeUpdate);
        TransactionEntry testTransactionEntry = transactionEntryList.get(transactionEntryList.size() - 1);
        assertThat(testTransactionEntry.getEntryAmount()).isEqualByComparingTo(UPDATED_ENTRY_AMOUNT);
        assertThat(testTransactionEntry.getTransactionEntryType()).isEqualTo(UPDATED_TRANSACTION_ENTRY_TYPE);
        assertThat(testTransactionEntry.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testTransactionEntry.getWasProposed()).isEqualTo(UPDATED_WAS_PROPOSED);
        assertThat(testTransactionEntry.getWasPosted()).isEqualTo(UPDATED_WAS_POSTED);
        assertThat(testTransactionEntry.getWasDeleted()).isEqualTo(UPDATED_WAS_DELETED);
        assertThat(testTransactionEntry.getWasApproved()).isEqualTo(UPDATED_WAS_APPROVED);
    }

    @Test
    void patchNonExistingTransactionEntry() throws Exception {
        int databaseSizeBeforeUpdate = transactionEntryRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionEntrySearchRepository.findAll().collectList().block());
        transactionEntry.setId(count.incrementAndGet());

        // Create the TransactionEntry
        TransactionEntryDTO transactionEntryDTO = transactionEntryMapper.toDto(transactionEntry);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, transactionEntryDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionEntryDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TransactionEntry in the database
        List<TransactionEntry> transactionEntryList = transactionEntryRepository.findAll().collectList().block();
        assertThat(transactionEntryList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionEntrySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithIdMismatchTransactionEntry() throws Exception {
        int databaseSizeBeforeUpdate = transactionEntryRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionEntrySearchRepository.findAll().collectList().block());
        transactionEntry.setId(count.incrementAndGet());

        // Create the TransactionEntry
        TransactionEntryDTO transactionEntryDTO = transactionEntryMapper.toDto(transactionEntry);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionEntryDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TransactionEntry in the database
        List<TransactionEntry> transactionEntryList = transactionEntryRepository.findAll().collectList().block();
        assertThat(transactionEntryList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionEntrySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithMissingIdPathParamTransactionEntry() throws Exception {
        int databaseSizeBeforeUpdate = transactionEntryRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionEntrySearchRepository.findAll().collectList().block());
        transactionEntry.setId(count.incrementAndGet());

        // Create the TransactionEntry
        TransactionEntryDTO transactionEntryDTO = transactionEntryMapper.toDto(transactionEntry);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionEntryDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the TransactionEntry in the database
        List<TransactionEntry> transactionEntryList = transactionEntryRepository.findAll().collectList().block();
        assertThat(transactionEntryList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionEntrySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void deleteTransactionEntry() {
        // Initialize the database
        transactionEntryRepository.save(transactionEntry).block();
        transactionEntryRepository.save(transactionEntry).block();
        transactionEntrySearchRepository.save(transactionEntry).block();

        int databaseSizeBeforeDelete = transactionEntryRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionEntrySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the transactionEntry
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, transactionEntry.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<TransactionEntry> transactionEntryList = transactionEntryRepository.findAll().collectList().block();
        assertThat(transactionEntryList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionEntrySearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    void searchTransactionEntry() {
        // Initialize the database
        transactionEntry = transactionEntryRepository.save(transactionEntry).block();
        transactionEntrySearchRepository.save(transactionEntry).block();

        // Search the transactionEntry
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + transactionEntry.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(transactionEntry.getId().intValue()))
            .jsonPath("$.[*].entryAmount")
            .value(hasItem(sameNumber(DEFAULT_ENTRY_AMOUNT)))
            .jsonPath("$.[*].transactionEntryType")
            .value(hasItem(DEFAULT_TRANSACTION_ENTRY_TYPE.toString()))
            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION))
            .jsonPath("$.[*].wasProposed")
            .value(hasItem(DEFAULT_WAS_PROPOSED.booleanValue()))
            .jsonPath("$.[*].wasPosted")
            .value(hasItem(DEFAULT_WAS_POSTED.booleanValue()))
            .jsonPath("$.[*].wasDeleted")
            .value(hasItem(DEFAULT_WAS_DELETED.booleanValue()))
            .jsonPath("$.[*].wasApproved")
            .value(hasItem(DEFAULT_WAS_APPROVED.booleanValue()));
    }
}
