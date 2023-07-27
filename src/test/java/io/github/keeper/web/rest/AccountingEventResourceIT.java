package io.github.keeper.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import io.github.keeper.IntegrationTest;
import io.github.keeper.domain.AccountingEvent;
import io.github.keeper.domain.Dealer;
import io.github.keeper.repository.AccountingEventRepository;
import io.github.keeper.repository.EntityManager;
import io.github.keeper.repository.search.AccountingEventSearchRepository;
import io.github.keeper.service.AccountingEventService;
import io.github.keeper.service.dto.AccountingEventDTO;
import io.github.keeper.service.mapper.AccountingEventMapper;
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
 * Integration tests for the {@link AccountingEventResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class AccountingEventResourceIT {

    private static final LocalDate DEFAULT_EVENT_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_EVENT_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String ENTITY_API_URL = "/api/accounting-events";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/accounting-events";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private AccountingEventRepository accountingEventRepository;

    @Mock
    private AccountingEventRepository accountingEventRepositoryMock;

    @Autowired
    private AccountingEventMapper accountingEventMapper;

    @Mock
    private AccountingEventService accountingEventServiceMock;

    @Autowired
    private AccountingEventSearchRepository accountingEventSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private AccountingEvent accountingEvent;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AccountingEvent createEntity(EntityManager em) {
        AccountingEvent accountingEvent = new AccountingEvent().eventDate(DEFAULT_EVENT_DATE);
        // Add required entity
        Dealer dealer;
        dealer = em.insert(DealerResourceIT.createEntity(em)).block();
        accountingEvent.setDealer(dealer);
        return accountingEvent;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AccountingEvent createUpdatedEntity(EntityManager em) {
        AccountingEvent accountingEvent = new AccountingEvent().eventDate(UPDATED_EVENT_DATE);
        // Add required entity
        Dealer dealer;
        dealer = em.insert(DealerResourceIT.createUpdatedEntity(em)).block();
        accountingEvent.setDealer(dealer);
        return accountingEvent;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(AccountingEvent.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
        DealerResourceIT.deleteEntities(em);
    }

    @AfterEach
    public void cleanup() {
        deleteEntities(em);
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        accountingEventSearchRepository.deleteAll().block();
        assertThat(accountingEventSearchRepository.count().block()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        accountingEvent = createEntity(em);
    }

    @Test
    void createAccountingEvent() throws Exception {
        int databaseSizeBeforeCreate = accountingEventRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(accountingEventSearchRepository.findAll().collectList().block());
        // Create the AccountingEvent
        AccountingEventDTO accountingEventDTO = accountingEventMapper.toDto(accountingEvent);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(accountingEventDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the AccountingEvent in the database
        List<AccountingEvent> accountingEventList = accountingEventRepository.findAll().collectList().block();
        assertThat(accountingEventList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(accountingEventSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        AccountingEvent testAccountingEvent = accountingEventList.get(accountingEventList.size() - 1);
        assertThat(testAccountingEvent.getEventDate()).isEqualTo(DEFAULT_EVENT_DATE);
    }

    @Test
    void createAccountingEventWithExistingId() throws Exception {
        // Create the AccountingEvent with an existing ID
        accountingEvent.setId(1L);
        AccountingEventDTO accountingEventDTO = accountingEventMapper.toDto(accountingEvent);

        int databaseSizeBeforeCreate = accountingEventRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(accountingEventSearchRepository.findAll().collectList().block());

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(accountingEventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the AccountingEvent in the database
        List<AccountingEvent> accountingEventList = accountingEventRepository.findAll().collectList().block();
        assertThat(accountingEventList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(accountingEventSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkEventDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = accountingEventRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(accountingEventSearchRepository.findAll().collectList().block());
        // set the field null
        accountingEvent.setEventDate(null);

        // Create the AccountingEvent, which fails.
        AccountingEventDTO accountingEventDTO = accountingEventMapper.toDto(accountingEvent);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(accountingEventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<AccountingEvent> accountingEventList = accountingEventRepository.findAll().collectList().block();
        assertThat(accountingEventList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(accountingEventSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void getAllAccountingEvents() {
        // Initialize the database
        accountingEventRepository.save(accountingEvent).block();

        // Get all the accountingEventList
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
            .value(hasItem(accountingEvent.getId().intValue()))
            .jsonPath("$.[*].eventDate")
            .value(hasItem(DEFAULT_EVENT_DATE.toString()));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAccountingEventsWithEagerRelationshipsIsEnabled() {
        when(accountingEventServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(accountingEventServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAccountingEventsWithEagerRelationshipsIsNotEnabled() {
        when(accountingEventServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=false").exchange().expectStatus().isOk();
        verify(accountingEventRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    void getAccountingEvent() {
        // Initialize the database
        accountingEventRepository.save(accountingEvent).block();

        // Get the accountingEvent
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, accountingEvent.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(accountingEvent.getId().intValue()))
            .jsonPath("$.eventDate")
            .value(is(DEFAULT_EVENT_DATE.toString()));
    }

    @Test
    void getNonExistingAccountingEvent() {
        // Get the accountingEvent
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingAccountingEvent() throws Exception {
        // Initialize the database
        accountingEventRepository.save(accountingEvent).block();

        int databaseSizeBeforeUpdate = accountingEventRepository.findAll().collectList().block().size();
        accountingEventSearchRepository.save(accountingEvent).block();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(accountingEventSearchRepository.findAll().collectList().block());

        // Update the accountingEvent
        AccountingEvent updatedAccountingEvent = accountingEventRepository.findById(accountingEvent.getId()).block();
        updatedAccountingEvent.eventDate(UPDATED_EVENT_DATE);
        AccountingEventDTO accountingEventDTO = accountingEventMapper.toDto(updatedAccountingEvent);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, accountingEventDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(accountingEventDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the AccountingEvent in the database
        List<AccountingEvent> accountingEventList = accountingEventRepository.findAll().collectList().block();
        assertThat(accountingEventList).hasSize(databaseSizeBeforeUpdate);
        AccountingEvent testAccountingEvent = accountingEventList.get(accountingEventList.size() - 1);
        assertThat(testAccountingEvent.getEventDate()).isEqualTo(UPDATED_EVENT_DATE);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(accountingEventSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<AccountingEvent> accountingEventSearchList = IterableUtils.toList(
                    accountingEventSearchRepository.findAll().collectList().block()
                );
                AccountingEvent testAccountingEventSearch = accountingEventSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testAccountingEventSearch.getEventDate()).isEqualTo(UPDATED_EVENT_DATE);
            });
    }

    @Test
    void putNonExistingAccountingEvent() throws Exception {
        int databaseSizeBeforeUpdate = accountingEventRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(accountingEventSearchRepository.findAll().collectList().block());
        accountingEvent.setId(count.incrementAndGet());

        // Create the AccountingEvent
        AccountingEventDTO accountingEventDTO = accountingEventMapper.toDto(accountingEvent);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, accountingEventDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(accountingEventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the AccountingEvent in the database
        List<AccountingEvent> accountingEventList = accountingEventRepository.findAll().collectList().block();
        assertThat(accountingEventList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(accountingEventSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithIdMismatchAccountingEvent() throws Exception {
        int databaseSizeBeforeUpdate = accountingEventRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(accountingEventSearchRepository.findAll().collectList().block());
        accountingEvent.setId(count.incrementAndGet());

        // Create the AccountingEvent
        AccountingEventDTO accountingEventDTO = accountingEventMapper.toDto(accountingEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(accountingEventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the AccountingEvent in the database
        List<AccountingEvent> accountingEventList = accountingEventRepository.findAll().collectList().block();
        assertThat(accountingEventList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(accountingEventSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithMissingIdPathParamAccountingEvent() throws Exception {
        int databaseSizeBeforeUpdate = accountingEventRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(accountingEventSearchRepository.findAll().collectList().block());
        accountingEvent.setId(count.incrementAndGet());

        // Create the AccountingEvent
        AccountingEventDTO accountingEventDTO = accountingEventMapper.toDto(accountingEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(accountingEventDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the AccountingEvent in the database
        List<AccountingEvent> accountingEventList = accountingEventRepository.findAll().collectList().block();
        assertThat(accountingEventList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(accountingEventSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void partialUpdateAccountingEventWithPatch() throws Exception {
        // Initialize the database
        accountingEventRepository.save(accountingEvent).block();

        int databaseSizeBeforeUpdate = accountingEventRepository.findAll().collectList().block().size();

        // Update the accountingEvent using partial update
        AccountingEvent partialUpdatedAccountingEvent = new AccountingEvent();
        partialUpdatedAccountingEvent.setId(accountingEvent.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedAccountingEvent.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedAccountingEvent))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the AccountingEvent in the database
        List<AccountingEvent> accountingEventList = accountingEventRepository.findAll().collectList().block();
        assertThat(accountingEventList).hasSize(databaseSizeBeforeUpdate);
        AccountingEvent testAccountingEvent = accountingEventList.get(accountingEventList.size() - 1);
        assertThat(testAccountingEvent.getEventDate()).isEqualTo(DEFAULT_EVENT_DATE);
    }

    @Test
    void fullUpdateAccountingEventWithPatch() throws Exception {
        // Initialize the database
        accountingEventRepository.save(accountingEvent).block();

        int databaseSizeBeforeUpdate = accountingEventRepository.findAll().collectList().block().size();

        // Update the accountingEvent using partial update
        AccountingEvent partialUpdatedAccountingEvent = new AccountingEvent();
        partialUpdatedAccountingEvent.setId(accountingEvent.getId());

        partialUpdatedAccountingEvent.eventDate(UPDATED_EVENT_DATE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedAccountingEvent.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedAccountingEvent))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the AccountingEvent in the database
        List<AccountingEvent> accountingEventList = accountingEventRepository.findAll().collectList().block();
        assertThat(accountingEventList).hasSize(databaseSizeBeforeUpdate);
        AccountingEvent testAccountingEvent = accountingEventList.get(accountingEventList.size() - 1);
        assertThat(testAccountingEvent.getEventDate()).isEqualTo(UPDATED_EVENT_DATE);
    }

    @Test
    void patchNonExistingAccountingEvent() throws Exception {
        int databaseSizeBeforeUpdate = accountingEventRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(accountingEventSearchRepository.findAll().collectList().block());
        accountingEvent.setId(count.incrementAndGet());

        // Create the AccountingEvent
        AccountingEventDTO accountingEventDTO = accountingEventMapper.toDto(accountingEvent);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, accountingEventDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(accountingEventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the AccountingEvent in the database
        List<AccountingEvent> accountingEventList = accountingEventRepository.findAll().collectList().block();
        assertThat(accountingEventList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(accountingEventSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithIdMismatchAccountingEvent() throws Exception {
        int databaseSizeBeforeUpdate = accountingEventRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(accountingEventSearchRepository.findAll().collectList().block());
        accountingEvent.setId(count.incrementAndGet());

        // Create the AccountingEvent
        AccountingEventDTO accountingEventDTO = accountingEventMapper.toDto(accountingEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(accountingEventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the AccountingEvent in the database
        List<AccountingEvent> accountingEventList = accountingEventRepository.findAll().collectList().block();
        assertThat(accountingEventList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(accountingEventSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithMissingIdPathParamAccountingEvent() throws Exception {
        int databaseSizeBeforeUpdate = accountingEventRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(accountingEventSearchRepository.findAll().collectList().block());
        accountingEvent.setId(count.incrementAndGet());

        // Create the AccountingEvent
        AccountingEventDTO accountingEventDTO = accountingEventMapper.toDto(accountingEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(accountingEventDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the AccountingEvent in the database
        List<AccountingEvent> accountingEventList = accountingEventRepository.findAll().collectList().block();
        assertThat(accountingEventList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(accountingEventSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void deleteAccountingEvent() {
        // Initialize the database
        accountingEventRepository.save(accountingEvent).block();
        accountingEventRepository.save(accountingEvent).block();
        accountingEventSearchRepository.save(accountingEvent).block();

        int databaseSizeBeforeDelete = accountingEventRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(accountingEventSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the accountingEvent
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, accountingEvent.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<AccountingEvent> accountingEventList = accountingEventRepository.findAll().collectList().block();
        assertThat(accountingEventList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(accountingEventSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    void searchAccountingEvent() {
        // Initialize the database
        accountingEvent = accountingEventRepository.save(accountingEvent).block();
        accountingEventSearchRepository.save(accountingEvent).block();

        // Search the accountingEvent
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + accountingEvent.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(accountingEvent.getId().intValue()))
            .jsonPath("$.[*].eventDate")
            .value(hasItem(DEFAULT_EVENT_DATE.toString()));
    }
}
