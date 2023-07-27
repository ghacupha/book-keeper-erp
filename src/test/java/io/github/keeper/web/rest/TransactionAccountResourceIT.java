package io.github.keeper.web.rest;

import static io.github.keeper.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import io.github.keeper.IntegrationTest;
import io.github.keeper.domain.TransactionAccount;
import io.github.keeper.domain.TransactionAccountType;
import io.github.keeper.domain.TransactionCurrency;
import io.github.keeper.repository.EntityManager;
import io.github.keeper.repository.TransactionAccountRepository;
import io.github.keeper.repository.search.TransactionAccountSearchRepository;
import io.github.keeper.service.TransactionAccountService;
import io.github.keeper.service.dto.TransactionAccountDTO;
import io.github.keeper.service.mapper.TransactionAccountMapper;
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
 * Integration tests for the {@link TransactionAccountResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class TransactionAccountResourceIT {

    private static final String DEFAULT_ACCOUNT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_ACCOUNT_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_ACCOUNT_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_ACCOUNT_NUMBER = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_OPENING_BALANCE = new BigDecimal(1);
    private static final BigDecimal UPDATED_OPENING_BALANCE = new BigDecimal(2);

    private static final String ENTITY_API_URL = "/api/transaction-accounts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/transaction-accounts";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private TransactionAccountRepository transactionAccountRepository;

    @Mock
    private TransactionAccountRepository transactionAccountRepositoryMock;

    @Autowired
    private TransactionAccountMapper transactionAccountMapper;

    @Mock
    private TransactionAccountService transactionAccountServiceMock;

    @Autowired
    private TransactionAccountSearchRepository transactionAccountSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private TransactionAccount transactionAccount;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TransactionAccount createEntity(EntityManager em) {
        TransactionAccount transactionAccount = new TransactionAccount()
            .accountName(DEFAULT_ACCOUNT_NAME)
            .accountNumber(DEFAULT_ACCOUNT_NUMBER)
            .openingBalance(DEFAULT_OPENING_BALANCE);
        // Add required entity
        TransactionAccountType transactionAccountType;
        transactionAccountType = em.insert(TransactionAccountTypeResourceIT.createEntity(em)).block();
        transactionAccount.setTransactionAccountType(transactionAccountType);
        // Add required entity
        TransactionCurrency transactionCurrency;
        transactionCurrency = em.insert(TransactionCurrencyResourceIT.createEntity(em)).block();
        transactionAccount.setTransactionCurrency(transactionCurrency);
        return transactionAccount;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TransactionAccount createUpdatedEntity(EntityManager em) {
        TransactionAccount transactionAccount = new TransactionAccount()
            .accountName(UPDATED_ACCOUNT_NAME)
            .accountNumber(UPDATED_ACCOUNT_NUMBER)
            .openingBalance(UPDATED_OPENING_BALANCE);
        // Add required entity
        TransactionAccountType transactionAccountType;
        transactionAccountType = em.insert(TransactionAccountTypeResourceIT.createUpdatedEntity(em)).block();
        transactionAccount.setTransactionAccountType(transactionAccountType);
        // Add required entity
        TransactionCurrency transactionCurrency;
        transactionCurrency = em.insert(TransactionCurrencyResourceIT.createUpdatedEntity(em)).block();
        transactionAccount.setTransactionCurrency(transactionCurrency);
        return transactionAccount;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(TransactionAccount.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
        TransactionAccountTypeResourceIT.deleteEntities(em);
        TransactionCurrencyResourceIT.deleteEntities(em);
    }

    @AfterEach
    public void cleanup() {
        deleteEntities(em);
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        transactionAccountSearchRepository.deleteAll().block();
        assertThat(transactionAccountSearchRepository.count().block()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        transactionAccount = createEntity(em);
    }

    @Test
    void createTransactionAccount() throws Exception {
        int databaseSizeBeforeCreate = transactionAccountRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionAccountSearchRepository.findAll().collectList().block());
        // Create the TransactionAccount
        TransactionAccountDTO transactionAccountDTO = transactionAccountMapper.toDto(transactionAccount);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionAccountDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the TransactionAccount in the database
        List<TransactionAccount> transactionAccountList = transactionAccountRepository.findAll().collectList().block();
        assertThat(transactionAccountList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionAccountSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        TransactionAccount testTransactionAccount = transactionAccountList.get(transactionAccountList.size() - 1);
        assertThat(testTransactionAccount.getAccountName()).isEqualTo(DEFAULT_ACCOUNT_NAME);
        assertThat(testTransactionAccount.getAccountNumber()).isEqualTo(DEFAULT_ACCOUNT_NUMBER);
        assertThat(testTransactionAccount.getOpeningBalance()).isEqualByComparingTo(DEFAULT_OPENING_BALANCE);
    }

    @Test
    void createTransactionAccountWithExistingId() throws Exception {
        // Create the TransactionAccount with an existing ID
        transactionAccount.setId(1L);
        TransactionAccountDTO transactionAccountDTO = transactionAccountMapper.toDto(transactionAccount);

        int databaseSizeBeforeCreate = transactionAccountRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionAccountSearchRepository.findAll().collectList().block());

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionAccountDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TransactionAccount in the database
        List<TransactionAccount> transactionAccountList = transactionAccountRepository.findAll().collectList().block();
        assertThat(transactionAccountList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionAccountSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkAccountNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = transactionAccountRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionAccountSearchRepository.findAll().collectList().block());
        // set the field null
        transactionAccount.setAccountName(null);

        // Create the TransactionAccount, which fails.
        TransactionAccountDTO transactionAccountDTO = transactionAccountMapper.toDto(transactionAccount);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionAccountDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<TransactionAccount> transactionAccountList = transactionAccountRepository.findAll().collectList().block();
        assertThat(transactionAccountList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionAccountSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void getAllTransactionAccounts() {
        // Initialize the database
        transactionAccountRepository.save(transactionAccount).block();

        // Get all the transactionAccountList
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
            .value(hasItem(transactionAccount.getId().intValue()))
            .jsonPath("$.[*].accountName")
            .value(hasItem(DEFAULT_ACCOUNT_NAME))
            .jsonPath("$.[*].accountNumber")
            .value(hasItem(DEFAULT_ACCOUNT_NUMBER))
            .jsonPath("$.[*].openingBalance")
            .value(hasItem(sameNumber(DEFAULT_OPENING_BALANCE)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllTransactionAccountsWithEagerRelationshipsIsEnabled() {
        when(transactionAccountServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(transactionAccountServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllTransactionAccountsWithEagerRelationshipsIsNotEnabled() {
        when(transactionAccountServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=false").exchange().expectStatus().isOk();
        verify(transactionAccountRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    void getTransactionAccount() {
        // Initialize the database
        transactionAccountRepository.save(transactionAccount).block();

        // Get the transactionAccount
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, transactionAccount.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(transactionAccount.getId().intValue()))
            .jsonPath("$.accountName")
            .value(is(DEFAULT_ACCOUNT_NAME))
            .jsonPath("$.accountNumber")
            .value(is(DEFAULT_ACCOUNT_NUMBER))
            .jsonPath("$.openingBalance")
            .value(is(sameNumber(DEFAULT_OPENING_BALANCE)));
    }

    @Test
    void getNonExistingTransactionAccount() {
        // Get the transactionAccount
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingTransactionAccount() throws Exception {
        // Initialize the database
        transactionAccountRepository.save(transactionAccount).block();

        int databaseSizeBeforeUpdate = transactionAccountRepository.findAll().collectList().block().size();
        transactionAccountSearchRepository.save(transactionAccount).block();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionAccountSearchRepository.findAll().collectList().block());

        // Update the transactionAccount
        TransactionAccount updatedTransactionAccount = transactionAccountRepository.findById(transactionAccount.getId()).block();
        updatedTransactionAccount
            .accountName(UPDATED_ACCOUNT_NAME)
            .accountNumber(UPDATED_ACCOUNT_NUMBER)
            .openingBalance(UPDATED_OPENING_BALANCE);
        TransactionAccountDTO transactionAccountDTO = transactionAccountMapper.toDto(updatedTransactionAccount);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, transactionAccountDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionAccountDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the TransactionAccount in the database
        List<TransactionAccount> transactionAccountList = transactionAccountRepository.findAll().collectList().block();
        assertThat(transactionAccountList).hasSize(databaseSizeBeforeUpdate);
        TransactionAccount testTransactionAccount = transactionAccountList.get(transactionAccountList.size() - 1);
        assertThat(testTransactionAccount.getAccountName()).isEqualTo(UPDATED_ACCOUNT_NAME);
        assertThat(testTransactionAccount.getAccountNumber()).isEqualTo(UPDATED_ACCOUNT_NUMBER);
        assertThat(testTransactionAccount.getOpeningBalance()).isEqualByComparingTo(UPDATED_OPENING_BALANCE);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionAccountSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<TransactionAccount> transactionAccountSearchList = IterableUtils.toList(
                    transactionAccountSearchRepository.findAll().collectList().block()
                );
                TransactionAccount testTransactionAccountSearch = transactionAccountSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testTransactionAccountSearch.getAccountName()).isEqualTo(UPDATED_ACCOUNT_NAME);
                assertThat(testTransactionAccountSearch.getAccountNumber()).isEqualTo(UPDATED_ACCOUNT_NUMBER);
                assertThat(testTransactionAccountSearch.getOpeningBalance()).isEqualByComparingTo(UPDATED_OPENING_BALANCE);
            });
    }

    @Test
    void putNonExistingTransactionAccount() throws Exception {
        int databaseSizeBeforeUpdate = transactionAccountRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionAccountSearchRepository.findAll().collectList().block());
        transactionAccount.setId(count.incrementAndGet());

        // Create the TransactionAccount
        TransactionAccountDTO transactionAccountDTO = transactionAccountMapper.toDto(transactionAccount);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, transactionAccountDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionAccountDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TransactionAccount in the database
        List<TransactionAccount> transactionAccountList = transactionAccountRepository.findAll().collectList().block();
        assertThat(transactionAccountList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionAccountSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithIdMismatchTransactionAccount() throws Exception {
        int databaseSizeBeforeUpdate = transactionAccountRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionAccountSearchRepository.findAll().collectList().block());
        transactionAccount.setId(count.incrementAndGet());

        // Create the TransactionAccount
        TransactionAccountDTO transactionAccountDTO = transactionAccountMapper.toDto(transactionAccount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionAccountDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TransactionAccount in the database
        List<TransactionAccount> transactionAccountList = transactionAccountRepository.findAll().collectList().block();
        assertThat(transactionAccountList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionAccountSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithMissingIdPathParamTransactionAccount() throws Exception {
        int databaseSizeBeforeUpdate = transactionAccountRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionAccountSearchRepository.findAll().collectList().block());
        transactionAccount.setId(count.incrementAndGet());

        // Create the TransactionAccount
        TransactionAccountDTO transactionAccountDTO = transactionAccountMapper.toDto(transactionAccount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionAccountDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the TransactionAccount in the database
        List<TransactionAccount> transactionAccountList = transactionAccountRepository.findAll().collectList().block();
        assertThat(transactionAccountList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionAccountSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void partialUpdateTransactionAccountWithPatch() throws Exception {
        // Initialize the database
        transactionAccountRepository.save(transactionAccount).block();

        int databaseSizeBeforeUpdate = transactionAccountRepository.findAll().collectList().block().size();

        // Update the transactionAccount using partial update
        TransactionAccount partialUpdatedTransactionAccount = new TransactionAccount();
        partialUpdatedTransactionAccount.setId(transactionAccount.getId());

        partialUpdatedTransactionAccount.openingBalance(UPDATED_OPENING_BALANCE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedTransactionAccount.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedTransactionAccount))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the TransactionAccount in the database
        List<TransactionAccount> transactionAccountList = transactionAccountRepository.findAll().collectList().block();
        assertThat(transactionAccountList).hasSize(databaseSizeBeforeUpdate);
        TransactionAccount testTransactionAccount = transactionAccountList.get(transactionAccountList.size() - 1);
        assertThat(testTransactionAccount.getAccountName()).isEqualTo(DEFAULT_ACCOUNT_NAME);
        assertThat(testTransactionAccount.getAccountNumber()).isEqualTo(DEFAULT_ACCOUNT_NUMBER);
        assertThat(testTransactionAccount.getOpeningBalance()).isEqualByComparingTo(UPDATED_OPENING_BALANCE);
    }

    @Test
    void fullUpdateTransactionAccountWithPatch() throws Exception {
        // Initialize the database
        transactionAccountRepository.save(transactionAccount).block();

        int databaseSizeBeforeUpdate = transactionAccountRepository.findAll().collectList().block().size();

        // Update the transactionAccount using partial update
        TransactionAccount partialUpdatedTransactionAccount = new TransactionAccount();
        partialUpdatedTransactionAccount.setId(transactionAccount.getId());

        partialUpdatedTransactionAccount
            .accountName(UPDATED_ACCOUNT_NAME)
            .accountNumber(UPDATED_ACCOUNT_NUMBER)
            .openingBalance(UPDATED_OPENING_BALANCE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedTransactionAccount.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedTransactionAccount))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the TransactionAccount in the database
        List<TransactionAccount> transactionAccountList = transactionAccountRepository.findAll().collectList().block();
        assertThat(transactionAccountList).hasSize(databaseSizeBeforeUpdate);
        TransactionAccount testTransactionAccount = transactionAccountList.get(transactionAccountList.size() - 1);
        assertThat(testTransactionAccount.getAccountName()).isEqualTo(UPDATED_ACCOUNT_NAME);
        assertThat(testTransactionAccount.getAccountNumber()).isEqualTo(UPDATED_ACCOUNT_NUMBER);
        assertThat(testTransactionAccount.getOpeningBalance()).isEqualByComparingTo(UPDATED_OPENING_BALANCE);
    }

    @Test
    void patchNonExistingTransactionAccount() throws Exception {
        int databaseSizeBeforeUpdate = transactionAccountRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionAccountSearchRepository.findAll().collectList().block());
        transactionAccount.setId(count.incrementAndGet());

        // Create the TransactionAccount
        TransactionAccountDTO transactionAccountDTO = transactionAccountMapper.toDto(transactionAccount);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, transactionAccountDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionAccountDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TransactionAccount in the database
        List<TransactionAccount> transactionAccountList = transactionAccountRepository.findAll().collectList().block();
        assertThat(transactionAccountList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionAccountSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithIdMismatchTransactionAccount() throws Exception {
        int databaseSizeBeforeUpdate = transactionAccountRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionAccountSearchRepository.findAll().collectList().block());
        transactionAccount.setId(count.incrementAndGet());

        // Create the TransactionAccount
        TransactionAccountDTO transactionAccountDTO = transactionAccountMapper.toDto(transactionAccount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionAccountDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TransactionAccount in the database
        List<TransactionAccount> transactionAccountList = transactionAccountRepository.findAll().collectList().block();
        assertThat(transactionAccountList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionAccountSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithMissingIdPathParamTransactionAccount() throws Exception {
        int databaseSizeBeforeUpdate = transactionAccountRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionAccountSearchRepository.findAll().collectList().block());
        transactionAccount.setId(count.incrementAndGet());

        // Create the TransactionAccount
        TransactionAccountDTO transactionAccountDTO = transactionAccountMapper.toDto(transactionAccount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(transactionAccountDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the TransactionAccount in the database
        List<TransactionAccount> transactionAccountList = transactionAccountRepository.findAll().collectList().block();
        assertThat(transactionAccountList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionAccountSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void deleteTransactionAccount() {
        // Initialize the database
        transactionAccountRepository.save(transactionAccount).block();
        transactionAccountRepository.save(transactionAccount).block();
        transactionAccountSearchRepository.save(transactionAccount).block();

        int databaseSizeBeforeDelete = transactionAccountRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(transactionAccountSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the transactionAccount
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, transactionAccount.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<TransactionAccount> transactionAccountList = transactionAccountRepository.findAll().collectList().block();
        assertThat(transactionAccountList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(transactionAccountSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    void searchTransactionAccount() {
        // Initialize the database
        transactionAccount = transactionAccountRepository.save(transactionAccount).block();
        transactionAccountSearchRepository.save(transactionAccount).block();

        // Search the transactionAccount
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + transactionAccount.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(transactionAccount.getId().intValue()))
            .jsonPath("$.[*].accountName")
            .value(hasItem(DEFAULT_ACCOUNT_NAME))
            .jsonPath("$.[*].accountNumber")
            .value(hasItem(DEFAULT_ACCOUNT_NUMBER))
            .jsonPath("$.[*].openingBalance")
            .value(hasItem(sameNumber(DEFAULT_OPENING_BALANCE)));
    }
}
