package io.github.keeper.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import io.github.keeper.IntegrationTest;
import io.github.keeper.domain.Dealer;
import io.github.keeper.domain.DealerType;
import io.github.keeper.repository.DealerRepository;
import io.github.keeper.repository.EntityManager;
import io.github.keeper.repository.search.DealerSearchRepository;
import io.github.keeper.service.DealerService;
import io.github.keeper.service.dto.DealerDTO;
import io.github.keeper.service.mapper.DealerMapper;
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
 * Integration tests for the {@link DealerResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class DealerResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/dealers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/dealers";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private DealerRepository dealerRepository;

    @Mock
    private DealerRepository dealerRepositoryMock;

    @Autowired
    private DealerMapper dealerMapper;

    @Mock
    private DealerService dealerServiceMock;

    @Autowired
    private DealerSearchRepository dealerSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Dealer dealer;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Dealer createEntity(EntityManager em) {
        Dealer dealer = new Dealer().name(DEFAULT_NAME);
        // Add required entity
        DealerType dealerType;
        dealerType = em.insert(DealerTypeResourceIT.createEntity(em)).block();
        dealer.setDealerType(dealerType);
        return dealer;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Dealer createUpdatedEntity(EntityManager em) {
        Dealer dealer = new Dealer().name(UPDATED_NAME);
        // Add required entity
        DealerType dealerType;
        dealerType = em.insert(DealerTypeResourceIT.createUpdatedEntity(em)).block();
        dealer.setDealerType(dealerType);
        return dealer;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Dealer.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
        DealerTypeResourceIT.deleteEntities(em);
    }

    @AfterEach
    public void cleanup() {
        deleteEntities(em);
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        dealerSearchRepository.deleteAll().block();
        assertThat(dealerSearchRepository.count().block()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        dealer = createEntity(em);
    }

    @Test
    void createDealer() throws Exception {
        int databaseSizeBeforeCreate = dealerRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dealerSearchRepository.findAll().collectList().block());
        // Create the Dealer
        DealerDTO dealerDTO = dealerMapper.toDto(dealer);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(dealerDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Dealer in the database
        List<Dealer> dealerList = dealerRepository.findAll().collectList().block();
        assertThat(dealerList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(dealerSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        Dealer testDealer = dealerList.get(dealerList.size() - 1);
        assertThat(testDealer.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    void createDealerWithExistingId() throws Exception {
        // Create the Dealer with an existing ID
        dealer.setId(1L);
        DealerDTO dealerDTO = dealerMapper.toDto(dealer);

        int databaseSizeBeforeCreate = dealerRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dealerSearchRepository.findAll().collectList().block());

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(dealerDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Dealer in the database
        List<Dealer> dealerList = dealerRepository.findAll().collectList().block();
        assertThat(dealerList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(dealerSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = dealerRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dealerSearchRepository.findAll().collectList().block());
        // set the field null
        dealer.setName(null);

        // Create the Dealer, which fails.
        DealerDTO dealerDTO = dealerMapper.toDto(dealer);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(dealerDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Dealer> dealerList = dealerRepository.findAll().collectList().block();
        assertThat(dealerList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(dealerSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void getAllDealers() {
        // Initialize the database
        dealerRepository.save(dealer).block();

        // Get all the dealerList
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
            .value(hasItem(dealer.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllDealersWithEagerRelationshipsIsEnabled() {
        when(dealerServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(dealerServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllDealersWithEagerRelationshipsIsNotEnabled() {
        when(dealerServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=false").exchange().expectStatus().isOk();
        verify(dealerRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    void getDealer() {
        // Initialize the database
        dealerRepository.save(dealer).block();

        // Get the dealer
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, dealer.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(dealer.getId().intValue()))
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME));
    }

    @Test
    void getNonExistingDealer() {
        // Get the dealer
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingDealer() throws Exception {
        // Initialize the database
        dealerRepository.save(dealer).block();

        int databaseSizeBeforeUpdate = dealerRepository.findAll().collectList().block().size();
        dealerSearchRepository.save(dealer).block();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dealerSearchRepository.findAll().collectList().block());

        // Update the dealer
        Dealer updatedDealer = dealerRepository.findById(dealer.getId()).block();
        updatedDealer.name(UPDATED_NAME);
        DealerDTO dealerDTO = dealerMapper.toDto(updatedDealer);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, dealerDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(dealerDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Dealer in the database
        List<Dealer> dealerList = dealerRepository.findAll().collectList().block();
        assertThat(dealerList).hasSize(databaseSizeBeforeUpdate);
        Dealer testDealer = dealerList.get(dealerList.size() - 1);
        assertThat(testDealer.getName()).isEqualTo(UPDATED_NAME);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(dealerSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Dealer> dealerSearchList = IterableUtils.toList(dealerSearchRepository.findAll().collectList().block());
                Dealer testDealerSearch = dealerSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testDealerSearch.getName()).isEqualTo(UPDATED_NAME);
            });
    }

    @Test
    void putNonExistingDealer() throws Exception {
        int databaseSizeBeforeUpdate = dealerRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dealerSearchRepository.findAll().collectList().block());
        dealer.setId(count.incrementAndGet());

        // Create the Dealer
        DealerDTO dealerDTO = dealerMapper.toDto(dealer);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, dealerDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(dealerDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Dealer in the database
        List<Dealer> dealerList = dealerRepository.findAll().collectList().block();
        assertThat(dealerList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(dealerSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithIdMismatchDealer() throws Exception {
        int databaseSizeBeforeUpdate = dealerRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dealerSearchRepository.findAll().collectList().block());
        dealer.setId(count.incrementAndGet());

        // Create the Dealer
        DealerDTO dealerDTO = dealerMapper.toDto(dealer);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(dealerDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Dealer in the database
        List<Dealer> dealerList = dealerRepository.findAll().collectList().block();
        assertThat(dealerList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(dealerSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithMissingIdPathParamDealer() throws Exception {
        int databaseSizeBeforeUpdate = dealerRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dealerSearchRepository.findAll().collectList().block());
        dealer.setId(count.incrementAndGet());

        // Create the Dealer
        DealerDTO dealerDTO = dealerMapper.toDto(dealer);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(dealerDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Dealer in the database
        List<Dealer> dealerList = dealerRepository.findAll().collectList().block();
        assertThat(dealerList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(dealerSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void partialUpdateDealerWithPatch() throws Exception {
        // Initialize the database
        dealerRepository.save(dealer).block();

        int databaseSizeBeforeUpdate = dealerRepository.findAll().collectList().block().size();

        // Update the dealer using partial update
        Dealer partialUpdatedDealer = new Dealer();
        partialUpdatedDealer.setId(dealer.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedDealer.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedDealer))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Dealer in the database
        List<Dealer> dealerList = dealerRepository.findAll().collectList().block();
        assertThat(dealerList).hasSize(databaseSizeBeforeUpdate);
        Dealer testDealer = dealerList.get(dealerList.size() - 1);
        assertThat(testDealer.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    void fullUpdateDealerWithPatch() throws Exception {
        // Initialize the database
        dealerRepository.save(dealer).block();

        int databaseSizeBeforeUpdate = dealerRepository.findAll().collectList().block().size();

        // Update the dealer using partial update
        Dealer partialUpdatedDealer = new Dealer();
        partialUpdatedDealer.setId(dealer.getId());

        partialUpdatedDealer.name(UPDATED_NAME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedDealer.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedDealer))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Dealer in the database
        List<Dealer> dealerList = dealerRepository.findAll().collectList().block();
        assertThat(dealerList).hasSize(databaseSizeBeforeUpdate);
        Dealer testDealer = dealerList.get(dealerList.size() - 1);
        assertThat(testDealer.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    void patchNonExistingDealer() throws Exception {
        int databaseSizeBeforeUpdate = dealerRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dealerSearchRepository.findAll().collectList().block());
        dealer.setId(count.incrementAndGet());

        // Create the Dealer
        DealerDTO dealerDTO = dealerMapper.toDto(dealer);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, dealerDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(dealerDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Dealer in the database
        List<Dealer> dealerList = dealerRepository.findAll().collectList().block();
        assertThat(dealerList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(dealerSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithIdMismatchDealer() throws Exception {
        int databaseSizeBeforeUpdate = dealerRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dealerSearchRepository.findAll().collectList().block());
        dealer.setId(count.incrementAndGet());

        // Create the Dealer
        DealerDTO dealerDTO = dealerMapper.toDto(dealer);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(dealerDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Dealer in the database
        List<Dealer> dealerList = dealerRepository.findAll().collectList().block();
        assertThat(dealerList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(dealerSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithMissingIdPathParamDealer() throws Exception {
        int databaseSizeBeforeUpdate = dealerRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dealerSearchRepository.findAll().collectList().block());
        dealer.setId(count.incrementAndGet());

        // Create the Dealer
        DealerDTO dealerDTO = dealerMapper.toDto(dealer);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(dealerDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Dealer in the database
        List<Dealer> dealerList = dealerRepository.findAll().collectList().block();
        assertThat(dealerList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(dealerSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void deleteDealer() {
        // Initialize the database
        dealerRepository.save(dealer).block();
        dealerRepository.save(dealer).block();
        dealerSearchRepository.save(dealer).block();

        int databaseSizeBeforeDelete = dealerRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(dealerSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the dealer
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, dealer.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Dealer> dealerList = dealerRepository.findAll().collectList().block();
        assertThat(dealerList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(dealerSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    void searchDealer() {
        // Initialize the database
        dealer = dealerRepository.save(dealer).block();
        dealerSearchRepository.save(dealer).block();

        // Search the dealer
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + dealer.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(dealer.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME));
    }
}
