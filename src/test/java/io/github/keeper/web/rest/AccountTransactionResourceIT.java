package io.github.keeper.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import io.github.keeper.IntegrationTest;
import io.github.keeper.domain.AccountTransaction;
import io.github.keeper.repository.AccountTransactionRepository;
import io.github.keeper.repository.EntityManager;
import io.github.keeper.repository.search.AccountTransactionSearchRepository;
import io.github.keeper.service.dto.AccountTransactionDTO;
import io.github.keeper.service.mapper.AccountTransactionMapper;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
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
 * Integration tests for the {@link AccountTransactionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class AccountTransactionResourceIT {

    private static final LocalDate DEFAULT_TRANSACTION_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_TRANSACTION_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_REFERENCE_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_REFERENCE_NUMBER = "BBBBBBBBBB";

    private static final Boolean DEFAULT_WAS_PROPOSED = false;
    private static final Boolean UPDATED_WAS_PROPOSED = true;

    private static final Boolean DEFAULT_WAS_POSTED = false;
    private static final Boolean UPDATED_WAS_POSTED = true;

    private static final Boolean DEFAULT_WAS_DELETED = false;
    private static final Boolean UPDATED_WAS_DELETED = true;

    private static final Boolean DEFAULT_WAS_APPROVED = false;
    private static final Boolean UPDATED_WAS_APPROVED = true;

    private static final String ENTITY_API_URL = "/api/account-transactions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/account-transactions";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private AccountTransactionRepository accountTransactionRepository;

    @Autowired
    private AccountTransactionMapper accountTransactionMapper;

    @Autowired
    private AccountTransactionSearchRepository accountTransactionSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private AccountTransaction accountTransaction;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AccountTransaction createEntity(EntityManager em) {
        AccountTransaction accountTransaction = new AccountTransaction()
            .transactionDate(DEFAULT_TRANSACTION_DATE)
            .description(DEFAULT_DESCRIPTION)
            .referenceNumber(DEFAULT_REFERENCE_NUMBER)
            .wasProposed(DEFAULT_WAS_PROPOSED)
            .wasPosted(DEFAULT_WAS_POSTED)
            .wasDeleted(DEFAULT_WAS_DELETED)
            .wasApproved(DEFAULT_WAS_APPROVED);
        return accountTransaction;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AccountTransaction createUpdatedEntity(EntityManager em) {
        AccountTransaction accountTransaction = new AccountTransaction()
            .transactionDate(UPDATED_TRANSACTION_DATE)
            .description(UPDATED_DESCRIPTION)
            .referenceNumber(UPDATED_REFERENCE_NUMBER)
            .wasProposed(UPDATED_WAS_PROPOSED)
            .wasPosted(UPDATED_WAS_POSTED)
            .wasDeleted(UPDATED_WAS_DELETED)
            .wasApproved(UPDATED_WAS_APPROVED);
        return accountTransaction;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(AccountTransaction.class).block();
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
        accountTransactionSearchRepository.deleteAll().block();
        assertThat(accountTransactionSearchRepository.count().block()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        accountTransaction = createEntity(em);
    }

    @Test
    void createAccountTransaction() throws Exception {
        int databaseSizeBeforeCreate = accountTransactionRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(accountTransactionSearchRepository.findAll().collectList().block());
        // Create the AccountTransaction
        AccountTransactionDTO accountTransactionDTO = accountTransactionMapper.toDto(accountTransaction);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(accountTransactionDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the AccountTransaction in the database
        List<AccountTransaction> accountTransactionList = accountTransactionRepository.findAll().collectList().block();
        assertThat(accountTransactionList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(accountTransactionSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        AccountTransaction testAccountTransaction = accountTransactionList.get(accountTransactionList.size() - 1);
        assertThat(testAccountTransaction.getTransactionDate()).isEqualTo(DEFAULT_TRANSACTION_DATE);
        assertThat(testAccountTransaction.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testAccountTransaction.getReferenceNumber()).isEqualTo(DEFAULT_REFERENCE_NUMBER);
        assertThat(testAccountTransaction.getWasProposed()).isEqualTo(DEFAULT_WAS_PROPOSED);
        assertThat(testAccountTransaction.getWasPosted()).isEqualTo(DEFAULT_WAS_POSTED);
        assertThat(testAccountTransaction.getWasDeleted()).isEqualTo(DEFAULT_WAS_DELETED);
        assertThat(testAccountTransaction.getWasApproved()).isEqualTo(DEFAULT_WAS_APPROVED);
    }

    @Test
    void createAccountTransactionWithExistingId() throws Exception {
        // Create the AccountTransaction with an existing ID
        accountTransaction.setId(1L);
        AccountTransactionDTO accountTransactionDTO = accountTransactionMapper.toDto(accountTransaction);

        int databaseSizeBeforeCreate = accountTransactionRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(accountTransactionSearchRepository.findAll().collectList().block());

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(accountTransactionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the AccountTransaction in the database
        List<AccountTransaction> accountTransactionList = accountTransactionRepository.findAll().collectList().block();
        assertThat(accountTransactionList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(accountTransactionSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkTransactionDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = accountTransactionRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(accountTransactionSearchRepository.findAll().collectList().block());
        // set the field null
        accountTransaction.setTransactionDate(null);

        // Create the AccountTransaction, which fails.
        AccountTransactionDTO accountTransactionDTO = accountTransactionMapper.toDto(accountTransaction);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(accountTransactionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<AccountTransaction> accountTransactionList = accountTransactionRepository.findAll().collectList().block();
        assertThat(accountTransactionList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(accountTransactionSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void getAllAccountTransactions() {
        // Initialize the database
        accountTransactionRepository.save(accountTransaction).block();

        // Get all the accountTransactionList
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
            .value(hasItem(accountTransaction.getId().intValue()))
            .jsonPath("$.[*].transactionDate")
            .value(hasItem(DEFAULT_TRANSACTION_DATE.toString()))
            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION))
            .jsonPath("$.[*].referenceNumber")
            .value(hasItem(DEFAULT_REFERENCE_NUMBER))
            .jsonPath("$.[*].wasProposed")
            .value(hasItem(DEFAULT_WAS_PROPOSED.booleanValue()))
            .jsonPath("$.[*].wasPosted")
            .value(hasItem(DEFAULT_WAS_POSTED.booleanValue()))
            .jsonPath("$.[*].wasDeleted")
            .value(hasItem(DEFAULT_WAS_DELETED.booleanValue()))
            .jsonPath("$.[*].wasApproved")
            .value(hasItem(DEFAULT_WAS_APPROVED.booleanValue()));
    }

    @Test
    void getAccountTransaction() {
        // Initialize the database
        accountTransactionRepository.save(accountTransaction).block();

        // Get the accountTransaction
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, accountTransaction.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(accountTransaction.getId().intValue()))
            .jsonPath("$.transactionDate")
            .value(is(DEFAULT_TRANSACTION_DATE.toString()))
            .jsonPath("$.description")
            .value(is(DEFAULT_DESCRIPTION))
            .jsonPath("$.referenceNumber")
            .value(is(DEFAULT_REFERENCE_NUMBER))
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
    void getNonExistingAccountTransaction() {
        // Get the accountTransaction
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingAccountTransaction() throws Exception {
        // Initialize the database
        accountTransactionRepository.save(accountTransaction).block();

        int databaseSizeBeforeUpdate = accountTransactionRepository.findAll().collectList().block().size();
        accountTransactionSearchRepository.save(accountTransaction).block();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(accountTransactionSearchRepository.findAll().collectList().block());

        // Update the accountTransaction
        AccountTransaction updatedAccountTransaction = accountTransactionRepository.findById(accountTransaction.getId()).block();
        updatedAccountTransaction
            .transactionDate(UPDATED_TRANSACTION_DATE)
            .description(UPDATED_DESCRIPTION)
            .referenceNumber(UPDATED_REFERENCE_NUMBER)
            .wasProposed(UPDATED_WAS_PROPOSED)
            .wasPosted(UPDATED_WAS_POSTED)
            .wasDeleted(UPDATED_WAS_DELETED)
            .wasApproved(UPDATED_WAS_APPROVED);
        AccountTransactionDTO accountTransactionDTO = accountTransactionMapper.toDto(updatedAccountTransaction);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, accountTransactionDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(accountTransactionDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the AccountTransaction in the database
        List<AccountTransaction> accountTransactionList = accountTransactionRepository.findAll().collectList().block();
        assertThat(accountTransactionList).hasSize(databaseSizeBeforeUpdate);
        AccountTransaction testAccountTransaction = accountTransactionList.get(accountTransactionList.size() - 1);
        assertThat(testAccountTransaction.getTransactionDate()).isEqualTo(UPDATED_TRANSACTION_DATE);
        assertThat(testAccountTransaction.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testAccountTransaction.getReferenceNumber()).isEqualTo(UPDATED_REFERENCE_NUMBER);
        assertThat(testAccountTransaction.getWasProposed()).isEqualTo(UPDATED_WAS_PROPOSED);
        assertThat(testAccountTransaction.getWasPosted()).isEqualTo(UPDATED_WAS_POSTED);
        assertThat(testAccountTransaction.getWasDeleted()).isEqualTo(UPDATED_WAS_DELETED);
        assertThat(testAccountTransaction.getWasApproved()).isEqualTo(UPDATED_WAS_APPROVED);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(accountTransactionSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<AccountTransaction> accountTransactionSearchList = IterableUtils.toList(
                    accountTransactionSearchRepository.findAll().collectList().block()
                );
                AccountTransaction testAccountTransactionSearch = accountTransactionSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testAccountTransactionSearch.getTransactionDate()).isEqualTo(UPDATED_TRANSACTION_DATE);
                assertThat(testAccountTransactionSearch.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
                assertThat(testAccountTransactionSearch.getReferenceNumber()).isEqualTo(UPDATED_REFERENCE_NUMBER);
                assertThat(testAccountTransactionSearch.getWasProposed()).isEqualTo(UPDATED_WAS_PROPOSED);
                assertThat(testAccountTransactionSearch.getWasPosted()).isEqualTo(UPDATED_WAS_POSTED);
                assertThat(testAccountTransactionSearch.getWasDeleted()).isEqualTo(UPDATED_WAS_DELETED);
                assertThat(testAccountTransactionSearch.getWasApproved()).isEqualTo(UPDATED_WAS_APPROVED);
            });
    }

    @Test
    void putNonExistingAccountTransaction() throws Exception {
        int databaseSizeBeforeUpdate = accountTransactionRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(accountTransactionSearchRepository.findAll().collectList().block());
        accountTransaction.setId(count.incrementAndGet());

        // Create the AccountTransaction
        AccountTransactionDTO accountTransactionDTO = accountTransactionMapper.toDto(accountTransaction);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, accountTransactionDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(accountTransactionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the AccountTransaction in the database
        List<AccountTransaction> accountTransactionList = accountTransactionRepository.findAll().collectList().block();
        assertThat(accountTransactionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(accountTransactionSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithIdMismatchAccountTransaction() throws Exception {
        int databaseSizeBeforeUpdate = accountTransactionRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(accountTransactionSearchRepository.findAll().collectList().block());
        accountTransaction.setId(count.incrementAndGet());

        // Create the AccountTransaction
        AccountTransactionDTO accountTransactionDTO = accountTransactionMapper.toDto(accountTransaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(accountTransactionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the AccountTransaction in the database
        List<AccountTransaction> accountTransactionList = accountTransactionRepository.findAll().collectList().block();
        assertThat(accountTransactionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(accountTransactionSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithMissingIdPathParamAccountTransaction() throws Exception {
        int databaseSizeBeforeUpdate = accountTransactionRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(accountTransactionSearchRepository.findAll().collectList().block());
        accountTransaction.setId(count.incrementAndGet());

        // Create the AccountTransaction
        AccountTransactionDTO accountTransactionDTO = accountTransactionMapper.toDto(accountTransaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(accountTransactionDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the AccountTransaction in the database
        List<AccountTransaction> accountTransactionList = accountTransactionRepository.findAll().collectList().block();
        assertThat(accountTransactionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(accountTransactionSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void partialUpdateAccountTransactionWithPatch() throws Exception {
        // Initialize the database
        accountTransactionRepository.save(accountTransaction).block();

        int databaseSizeBeforeUpdate = accountTransactionRepository.findAll().collectList().block().size();

        // Update the accountTransaction using partial update
        AccountTransaction partialUpdatedAccountTransaction = new AccountTransaction();
        partialUpdatedAccountTransaction.setId(accountTransaction.getId());

        partialUpdatedAccountTransaction
            .transactionDate(UPDATED_TRANSACTION_DATE)
            .referenceNumber(UPDATED_REFERENCE_NUMBER)
            .wasPosted(UPDATED_WAS_POSTED)
            .wasApproved(UPDATED_WAS_APPROVED);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedAccountTransaction.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedAccountTransaction))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the AccountTransaction in the database
        List<AccountTransaction> accountTransactionList = accountTransactionRepository.findAll().collectList().block();
        assertThat(accountTransactionList).hasSize(databaseSizeBeforeUpdate);
        AccountTransaction testAccountTransaction = accountTransactionList.get(accountTransactionList.size() - 1);
        assertThat(testAccountTransaction.getTransactionDate()).isEqualTo(UPDATED_TRANSACTION_DATE);
        assertThat(testAccountTransaction.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testAccountTransaction.getReferenceNumber()).isEqualTo(UPDATED_REFERENCE_NUMBER);
        assertThat(testAccountTransaction.getWasProposed()).isEqualTo(DEFAULT_WAS_PROPOSED);
        assertThat(testAccountTransaction.getWasPosted()).isEqualTo(UPDATED_WAS_POSTED);
        assertThat(testAccountTransaction.getWasDeleted()).isEqualTo(DEFAULT_WAS_DELETED);
        assertThat(testAccountTransaction.getWasApproved()).isEqualTo(UPDATED_WAS_APPROVED);
    }

    @Test
    void fullUpdateAccountTransactionWithPatch() throws Exception {
        // Initialize the database
        accountTransactionRepository.save(accountTransaction).block();

        int databaseSizeBeforeUpdate = accountTransactionRepository.findAll().collectList().block().size();

        // Update the accountTransaction using partial update
        AccountTransaction partialUpdatedAccountTransaction = new AccountTransaction();
        partialUpdatedAccountTransaction.setId(accountTransaction.getId());

        partialUpdatedAccountTransaction
            .transactionDate(UPDATED_TRANSACTION_DATE)
            .description(UPDATED_DESCRIPTION)
            .referenceNumber(UPDATED_REFERENCE_NUMBER)
            .wasProposed(UPDATED_WAS_PROPOSED)
            .wasPosted(UPDATED_WAS_POSTED)
            .wasDeleted(UPDATED_WAS_DELETED)
            .wasApproved(UPDATED_WAS_APPROVED);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedAccountTransaction.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedAccountTransaction))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the AccountTransaction in the database
        List<AccountTransaction> accountTransactionList = accountTransactionRepository.findAll().collectList().block();
        assertThat(accountTransactionList).hasSize(databaseSizeBeforeUpdate);
        AccountTransaction testAccountTransaction = accountTransactionList.get(accountTransactionList.size() - 1);
        assertThat(testAccountTransaction.getTransactionDate()).isEqualTo(UPDATED_TRANSACTION_DATE);
        assertThat(testAccountTransaction.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testAccountTransaction.getReferenceNumber()).isEqualTo(UPDATED_REFERENCE_NUMBER);
        assertThat(testAccountTransaction.getWasProposed()).isEqualTo(UPDATED_WAS_PROPOSED);
        assertThat(testAccountTransaction.getWasPosted()).isEqualTo(UPDATED_WAS_POSTED);
        assertThat(testAccountTransaction.getWasDeleted()).isEqualTo(UPDATED_WAS_DELETED);
        assertThat(testAccountTransaction.getWasApproved()).isEqualTo(UPDATED_WAS_APPROVED);
    }

    @Test
    void patchNonExistingAccountTransaction() throws Exception {
        int databaseSizeBeforeUpdate = accountTransactionRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(accountTransactionSearchRepository.findAll().collectList().block());
        accountTransaction.setId(count.incrementAndGet());

        // Create the AccountTransaction
        AccountTransactionDTO accountTransactionDTO = accountTransactionMapper.toDto(accountTransaction);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, accountTransactionDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(accountTransactionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the AccountTransaction in the database
        List<AccountTransaction> accountTransactionList = accountTransactionRepository.findAll().collectList().block();
        assertThat(accountTransactionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(accountTransactionSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithIdMismatchAccountTransaction() throws Exception {
        int databaseSizeBeforeUpdate = accountTransactionRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(accountTransactionSearchRepository.findAll().collectList().block());
        accountTransaction.setId(count.incrementAndGet());

        // Create the AccountTransaction
        AccountTransactionDTO accountTransactionDTO = accountTransactionMapper.toDto(accountTransaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(accountTransactionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the AccountTransaction in the database
        List<AccountTransaction> accountTransactionList = accountTransactionRepository.findAll().collectList().block();
        assertThat(accountTransactionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(accountTransactionSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithMissingIdPathParamAccountTransaction() throws Exception {
        int databaseSizeBeforeUpdate = accountTransactionRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(accountTransactionSearchRepository.findAll().collectList().block());
        accountTransaction.setId(count.incrementAndGet());

        // Create the AccountTransaction
        AccountTransactionDTO accountTransactionDTO = accountTransactionMapper.toDto(accountTransaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(accountTransactionDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the AccountTransaction in the database
        List<AccountTransaction> accountTransactionList = accountTransactionRepository.findAll().collectList().block();
        assertThat(accountTransactionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(accountTransactionSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void deleteAccountTransaction() {
        // Initialize the database
        accountTransactionRepository.save(accountTransaction).block();
        accountTransactionRepository.save(accountTransaction).block();
        accountTransactionSearchRepository.save(accountTransaction).block();

        int databaseSizeBeforeDelete = accountTransactionRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(accountTransactionSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the accountTransaction
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, accountTransaction.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<AccountTransaction> accountTransactionList = accountTransactionRepository.findAll().collectList().block();
        assertThat(accountTransactionList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(accountTransactionSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    void searchAccountTransaction() {
        // Initialize the database
        accountTransaction = accountTransactionRepository.save(accountTransaction).block();
        accountTransactionSearchRepository.save(accountTransaction).block();

        // Search the accountTransaction
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + accountTransaction.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(accountTransaction.getId().intValue()))
            .jsonPath("$.[*].transactionDate")
            .value(hasItem(DEFAULT_TRANSACTION_DATE.toString()))
            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION))
            .jsonPath("$.[*].referenceNumber")
            .value(hasItem(DEFAULT_REFERENCE_NUMBER))
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
