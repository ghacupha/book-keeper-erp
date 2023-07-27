package io.github.keeper.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import io.github.keeper.IntegrationTest;
import io.github.keeper.domain.BalanceSheetItemType;
import io.github.keeper.domain.TransactionAccount;
import io.github.keeper.repository.BalanceSheetItemTypeRepository;
import io.github.keeper.repository.EntityManager;
import io.github.keeper.repository.search.BalanceSheetItemTypeSearchRepository;
import io.github.keeper.service.BalanceSheetItemTypeService;
import io.github.keeper.service.dto.BalanceSheetItemTypeDTO;
import io.github.keeper.service.mapper.BalanceSheetItemTypeMapper;
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
 * Integration tests for the {@link BalanceSheetItemTypeResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class BalanceSheetItemTypeResourceIT {

    private static final Integer DEFAULT_ITEM_SEQUENCE = 1;
    private static final Integer UPDATED_ITEM_SEQUENCE = 2;

    private static final String DEFAULT_ITEM_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_ITEM_NUMBER = "BBBBBBBBBB";

    private static final String DEFAULT_SHORT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_SHORT_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/balance-sheet-item-types";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/balance-sheet-item-types";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private BalanceSheetItemTypeRepository balanceSheetItemTypeRepository;

    @Mock
    private BalanceSheetItemTypeRepository balanceSheetItemTypeRepositoryMock;

    @Autowired
    private BalanceSheetItemTypeMapper balanceSheetItemTypeMapper;

    @Mock
    private BalanceSheetItemTypeService balanceSheetItemTypeServiceMock;

    @Autowired
    private BalanceSheetItemTypeSearchRepository balanceSheetItemTypeSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private BalanceSheetItemType balanceSheetItemType;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BalanceSheetItemType createEntity(EntityManager em) {
        BalanceSheetItemType balanceSheetItemType = new BalanceSheetItemType()
            .itemSequence(DEFAULT_ITEM_SEQUENCE)
            .itemNumber(DEFAULT_ITEM_NUMBER)
            .shortDescription(DEFAULT_SHORT_DESCRIPTION);
        // Add required entity
        TransactionAccount transactionAccount;
        transactionAccount = em.insert(TransactionAccountResourceIT.createEntity(em)).block();
        balanceSheetItemType.setTransactionAccount(transactionAccount);
        return balanceSheetItemType;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BalanceSheetItemType createUpdatedEntity(EntityManager em) {
        BalanceSheetItemType balanceSheetItemType = new BalanceSheetItemType()
            .itemSequence(UPDATED_ITEM_SEQUENCE)
            .itemNumber(UPDATED_ITEM_NUMBER)
            .shortDescription(UPDATED_SHORT_DESCRIPTION);
        // Add required entity
        TransactionAccount transactionAccount;
        transactionAccount = em.insert(TransactionAccountResourceIT.createUpdatedEntity(em)).block();
        balanceSheetItemType.setTransactionAccount(transactionAccount);
        return balanceSheetItemType;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(BalanceSheetItemType.class).block();
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
        balanceSheetItemTypeSearchRepository.deleteAll().block();
        assertThat(balanceSheetItemTypeSearchRepository.count().block()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        balanceSheetItemType = createEntity(em);
    }

    @Test
    void createBalanceSheetItemType() throws Exception {
        int databaseSizeBeforeCreate = balanceSheetItemTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(balanceSheetItemTypeSearchRepository.findAll().collectList().block());
        // Create the BalanceSheetItemType
        BalanceSheetItemTypeDTO balanceSheetItemTypeDTO = balanceSheetItemTypeMapper.toDto(balanceSheetItemType);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(balanceSheetItemTypeDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the BalanceSheetItemType in the database
        List<BalanceSheetItemType> balanceSheetItemTypeList = balanceSheetItemTypeRepository.findAll().collectList().block();
        assertThat(balanceSheetItemTypeList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(balanceSheetItemTypeSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        BalanceSheetItemType testBalanceSheetItemType = balanceSheetItemTypeList.get(balanceSheetItemTypeList.size() - 1);
        assertThat(testBalanceSheetItemType.getItemSequence()).isEqualTo(DEFAULT_ITEM_SEQUENCE);
        assertThat(testBalanceSheetItemType.getItemNumber()).isEqualTo(DEFAULT_ITEM_NUMBER);
        assertThat(testBalanceSheetItemType.getShortDescription()).isEqualTo(DEFAULT_SHORT_DESCRIPTION);
    }

    @Test
    void createBalanceSheetItemTypeWithExistingId() throws Exception {
        // Create the BalanceSheetItemType with an existing ID
        balanceSheetItemType.setId(1L);
        BalanceSheetItemTypeDTO balanceSheetItemTypeDTO = balanceSheetItemTypeMapper.toDto(balanceSheetItemType);

        int databaseSizeBeforeCreate = balanceSheetItemTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(balanceSheetItemTypeSearchRepository.findAll().collectList().block());

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(balanceSheetItemTypeDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the BalanceSheetItemType in the database
        List<BalanceSheetItemType> balanceSheetItemTypeList = balanceSheetItemTypeRepository.findAll().collectList().block();
        assertThat(balanceSheetItemTypeList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(balanceSheetItemTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkItemSequenceIsRequired() throws Exception {
        int databaseSizeBeforeTest = balanceSheetItemTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(balanceSheetItemTypeSearchRepository.findAll().collectList().block());
        // set the field null
        balanceSheetItemType.setItemSequence(null);

        // Create the BalanceSheetItemType, which fails.
        BalanceSheetItemTypeDTO balanceSheetItemTypeDTO = balanceSheetItemTypeMapper.toDto(balanceSheetItemType);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(balanceSheetItemTypeDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<BalanceSheetItemType> balanceSheetItemTypeList = balanceSheetItemTypeRepository.findAll().collectList().block();
        assertThat(balanceSheetItemTypeList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(balanceSheetItemTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkItemNumberIsRequired() throws Exception {
        int databaseSizeBeforeTest = balanceSheetItemTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(balanceSheetItemTypeSearchRepository.findAll().collectList().block());
        // set the field null
        balanceSheetItemType.setItemNumber(null);

        // Create the BalanceSheetItemType, which fails.
        BalanceSheetItemTypeDTO balanceSheetItemTypeDTO = balanceSheetItemTypeMapper.toDto(balanceSheetItemType);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(balanceSheetItemTypeDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<BalanceSheetItemType> balanceSheetItemTypeList = balanceSheetItemTypeRepository.findAll().collectList().block();
        assertThat(balanceSheetItemTypeList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(balanceSheetItemTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void getAllBalanceSheetItemTypes() {
        // Initialize the database
        balanceSheetItemTypeRepository.save(balanceSheetItemType).block();

        // Get all the balanceSheetItemTypeList
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
            .value(hasItem(balanceSheetItemType.getId().intValue()))
            .jsonPath("$.[*].itemSequence")
            .value(hasItem(DEFAULT_ITEM_SEQUENCE))
            .jsonPath("$.[*].itemNumber")
            .value(hasItem(DEFAULT_ITEM_NUMBER))
            .jsonPath("$.[*].shortDescription")
            .value(hasItem(DEFAULT_SHORT_DESCRIPTION));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllBalanceSheetItemTypesWithEagerRelationshipsIsEnabled() {
        when(balanceSheetItemTypeServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(balanceSheetItemTypeServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllBalanceSheetItemTypesWithEagerRelationshipsIsNotEnabled() {
        when(balanceSheetItemTypeServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=false").exchange().expectStatus().isOk();
        verify(balanceSheetItemTypeRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    void getBalanceSheetItemType() {
        // Initialize the database
        balanceSheetItemTypeRepository.save(balanceSheetItemType).block();

        // Get the balanceSheetItemType
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, balanceSheetItemType.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(balanceSheetItemType.getId().intValue()))
            .jsonPath("$.itemSequence")
            .value(is(DEFAULT_ITEM_SEQUENCE))
            .jsonPath("$.itemNumber")
            .value(is(DEFAULT_ITEM_NUMBER))
            .jsonPath("$.shortDescription")
            .value(is(DEFAULT_SHORT_DESCRIPTION));
    }

    @Test
    void getNonExistingBalanceSheetItemType() {
        // Get the balanceSheetItemType
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingBalanceSheetItemType() throws Exception {
        // Initialize the database
        balanceSheetItemTypeRepository.save(balanceSheetItemType).block();

        int databaseSizeBeforeUpdate = balanceSheetItemTypeRepository.findAll().collectList().block().size();
        balanceSheetItemTypeSearchRepository.save(balanceSheetItemType).block();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(balanceSheetItemTypeSearchRepository.findAll().collectList().block());

        // Update the balanceSheetItemType
        BalanceSheetItemType updatedBalanceSheetItemType = balanceSheetItemTypeRepository.findById(balanceSheetItemType.getId()).block();
        updatedBalanceSheetItemType
            .itemSequence(UPDATED_ITEM_SEQUENCE)
            .itemNumber(UPDATED_ITEM_NUMBER)
            .shortDescription(UPDATED_SHORT_DESCRIPTION);
        BalanceSheetItemTypeDTO balanceSheetItemTypeDTO = balanceSheetItemTypeMapper.toDto(updatedBalanceSheetItemType);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, balanceSheetItemTypeDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(balanceSheetItemTypeDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the BalanceSheetItemType in the database
        List<BalanceSheetItemType> balanceSheetItemTypeList = balanceSheetItemTypeRepository.findAll().collectList().block();
        assertThat(balanceSheetItemTypeList).hasSize(databaseSizeBeforeUpdate);
        BalanceSheetItemType testBalanceSheetItemType = balanceSheetItemTypeList.get(balanceSheetItemTypeList.size() - 1);
        assertThat(testBalanceSheetItemType.getItemSequence()).isEqualTo(UPDATED_ITEM_SEQUENCE);
        assertThat(testBalanceSheetItemType.getItemNumber()).isEqualTo(UPDATED_ITEM_NUMBER);
        assertThat(testBalanceSheetItemType.getShortDescription()).isEqualTo(UPDATED_SHORT_DESCRIPTION);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(balanceSheetItemTypeSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<BalanceSheetItemType> balanceSheetItemTypeSearchList = IterableUtils.toList(
                    balanceSheetItemTypeSearchRepository.findAll().collectList().block()
                );
                BalanceSheetItemType testBalanceSheetItemTypeSearch = balanceSheetItemTypeSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testBalanceSheetItemTypeSearch.getItemSequence()).isEqualTo(UPDATED_ITEM_SEQUENCE);
                assertThat(testBalanceSheetItemTypeSearch.getItemNumber()).isEqualTo(UPDATED_ITEM_NUMBER);
                assertThat(testBalanceSheetItemTypeSearch.getShortDescription()).isEqualTo(UPDATED_SHORT_DESCRIPTION);
            });
    }

    @Test
    void putNonExistingBalanceSheetItemType() throws Exception {
        int databaseSizeBeforeUpdate = balanceSheetItemTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(balanceSheetItemTypeSearchRepository.findAll().collectList().block());
        balanceSheetItemType.setId(count.incrementAndGet());

        // Create the BalanceSheetItemType
        BalanceSheetItemTypeDTO balanceSheetItemTypeDTO = balanceSheetItemTypeMapper.toDto(balanceSheetItemType);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, balanceSheetItemTypeDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(balanceSheetItemTypeDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the BalanceSheetItemType in the database
        List<BalanceSheetItemType> balanceSheetItemTypeList = balanceSheetItemTypeRepository.findAll().collectList().block();
        assertThat(balanceSheetItemTypeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(balanceSheetItemTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithIdMismatchBalanceSheetItemType() throws Exception {
        int databaseSizeBeforeUpdate = balanceSheetItemTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(balanceSheetItemTypeSearchRepository.findAll().collectList().block());
        balanceSheetItemType.setId(count.incrementAndGet());

        // Create the BalanceSheetItemType
        BalanceSheetItemTypeDTO balanceSheetItemTypeDTO = balanceSheetItemTypeMapper.toDto(balanceSheetItemType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(balanceSheetItemTypeDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the BalanceSheetItemType in the database
        List<BalanceSheetItemType> balanceSheetItemTypeList = balanceSheetItemTypeRepository.findAll().collectList().block();
        assertThat(balanceSheetItemTypeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(balanceSheetItemTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithMissingIdPathParamBalanceSheetItemType() throws Exception {
        int databaseSizeBeforeUpdate = balanceSheetItemTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(balanceSheetItemTypeSearchRepository.findAll().collectList().block());
        balanceSheetItemType.setId(count.incrementAndGet());

        // Create the BalanceSheetItemType
        BalanceSheetItemTypeDTO balanceSheetItemTypeDTO = balanceSheetItemTypeMapper.toDto(balanceSheetItemType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(balanceSheetItemTypeDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the BalanceSheetItemType in the database
        List<BalanceSheetItemType> balanceSheetItemTypeList = balanceSheetItemTypeRepository.findAll().collectList().block();
        assertThat(balanceSheetItemTypeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(balanceSheetItemTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void partialUpdateBalanceSheetItemTypeWithPatch() throws Exception {
        // Initialize the database
        balanceSheetItemTypeRepository.save(balanceSheetItemType).block();

        int databaseSizeBeforeUpdate = balanceSheetItemTypeRepository.findAll().collectList().block().size();

        // Update the balanceSheetItemType using partial update
        BalanceSheetItemType partialUpdatedBalanceSheetItemType = new BalanceSheetItemType();
        partialUpdatedBalanceSheetItemType.setId(balanceSheetItemType.getId());

        partialUpdatedBalanceSheetItemType
            .itemSequence(UPDATED_ITEM_SEQUENCE)
            .itemNumber(UPDATED_ITEM_NUMBER)
            .shortDescription(UPDATED_SHORT_DESCRIPTION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedBalanceSheetItemType.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedBalanceSheetItemType))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the BalanceSheetItemType in the database
        List<BalanceSheetItemType> balanceSheetItemTypeList = balanceSheetItemTypeRepository.findAll().collectList().block();
        assertThat(balanceSheetItemTypeList).hasSize(databaseSizeBeforeUpdate);
        BalanceSheetItemType testBalanceSheetItemType = balanceSheetItemTypeList.get(balanceSheetItemTypeList.size() - 1);
        assertThat(testBalanceSheetItemType.getItemSequence()).isEqualTo(UPDATED_ITEM_SEQUENCE);
        assertThat(testBalanceSheetItemType.getItemNumber()).isEqualTo(UPDATED_ITEM_NUMBER);
        assertThat(testBalanceSheetItemType.getShortDescription()).isEqualTo(UPDATED_SHORT_DESCRIPTION);
    }

    @Test
    void fullUpdateBalanceSheetItemTypeWithPatch() throws Exception {
        // Initialize the database
        balanceSheetItemTypeRepository.save(balanceSheetItemType).block();

        int databaseSizeBeforeUpdate = balanceSheetItemTypeRepository.findAll().collectList().block().size();

        // Update the balanceSheetItemType using partial update
        BalanceSheetItemType partialUpdatedBalanceSheetItemType = new BalanceSheetItemType();
        partialUpdatedBalanceSheetItemType.setId(balanceSheetItemType.getId());

        partialUpdatedBalanceSheetItemType
            .itemSequence(UPDATED_ITEM_SEQUENCE)
            .itemNumber(UPDATED_ITEM_NUMBER)
            .shortDescription(UPDATED_SHORT_DESCRIPTION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedBalanceSheetItemType.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedBalanceSheetItemType))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the BalanceSheetItemType in the database
        List<BalanceSheetItemType> balanceSheetItemTypeList = balanceSheetItemTypeRepository.findAll().collectList().block();
        assertThat(balanceSheetItemTypeList).hasSize(databaseSizeBeforeUpdate);
        BalanceSheetItemType testBalanceSheetItemType = balanceSheetItemTypeList.get(balanceSheetItemTypeList.size() - 1);
        assertThat(testBalanceSheetItemType.getItemSequence()).isEqualTo(UPDATED_ITEM_SEQUENCE);
        assertThat(testBalanceSheetItemType.getItemNumber()).isEqualTo(UPDATED_ITEM_NUMBER);
        assertThat(testBalanceSheetItemType.getShortDescription()).isEqualTo(UPDATED_SHORT_DESCRIPTION);
    }

    @Test
    void patchNonExistingBalanceSheetItemType() throws Exception {
        int databaseSizeBeforeUpdate = balanceSheetItemTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(balanceSheetItemTypeSearchRepository.findAll().collectList().block());
        balanceSheetItemType.setId(count.incrementAndGet());

        // Create the BalanceSheetItemType
        BalanceSheetItemTypeDTO balanceSheetItemTypeDTO = balanceSheetItemTypeMapper.toDto(balanceSheetItemType);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, balanceSheetItemTypeDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(balanceSheetItemTypeDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the BalanceSheetItemType in the database
        List<BalanceSheetItemType> balanceSheetItemTypeList = balanceSheetItemTypeRepository.findAll().collectList().block();
        assertThat(balanceSheetItemTypeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(balanceSheetItemTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithIdMismatchBalanceSheetItemType() throws Exception {
        int databaseSizeBeforeUpdate = balanceSheetItemTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(balanceSheetItemTypeSearchRepository.findAll().collectList().block());
        balanceSheetItemType.setId(count.incrementAndGet());

        // Create the BalanceSheetItemType
        BalanceSheetItemTypeDTO balanceSheetItemTypeDTO = balanceSheetItemTypeMapper.toDto(balanceSheetItemType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(balanceSheetItemTypeDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the BalanceSheetItemType in the database
        List<BalanceSheetItemType> balanceSheetItemTypeList = balanceSheetItemTypeRepository.findAll().collectList().block();
        assertThat(balanceSheetItemTypeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(balanceSheetItemTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithMissingIdPathParamBalanceSheetItemType() throws Exception {
        int databaseSizeBeforeUpdate = balanceSheetItemTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(balanceSheetItemTypeSearchRepository.findAll().collectList().block());
        balanceSheetItemType.setId(count.incrementAndGet());

        // Create the BalanceSheetItemType
        BalanceSheetItemTypeDTO balanceSheetItemTypeDTO = balanceSheetItemTypeMapper.toDto(balanceSheetItemType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(balanceSheetItemTypeDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the BalanceSheetItemType in the database
        List<BalanceSheetItemType> balanceSheetItemTypeList = balanceSheetItemTypeRepository.findAll().collectList().block();
        assertThat(balanceSheetItemTypeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(balanceSheetItemTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void deleteBalanceSheetItemType() {
        // Initialize the database
        balanceSheetItemTypeRepository.save(balanceSheetItemType).block();
        balanceSheetItemTypeRepository.save(balanceSheetItemType).block();
        balanceSheetItemTypeSearchRepository.save(balanceSheetItemType).block();

        int databaseSizeBeforeDelete = balanceSheetItemTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(balanceSheetItemTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the balanceSheetItemType
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, balanceSheetItemType.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<BalanceSheetItemType> balanceSheetItemTypeList = balanceSheetItemTypeRepository.findAll().collectList().block();
        assertThat(balanceSheetItemTypeList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(balanceSheetItemTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    void searchBalanceSheetItemType() {
        // Initialize the database
        balanceSheetItemType = balanceSheetItemTypeRepository.save(balanceSheetItemType).block();
        balanceSheetItemTypeSearchRepository.save(balanceSheetItemType).block();

        // Search the balanceSheetItemType
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + balanceSheetItemType.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(balanceSheetItemType.getId().intValue()))
            .jsonPath("$.[*].itemSequence")
            .value(hasItem(DEFAULT_ITEM_SEQUENCE))
            .jsonPath("$.[*].itemNumber")
            .value(hasItem(DEFAULT_ITEM_NUMBER))
            .jsonPath("$.[*].shortDescription")
            .value(hasItem(DEFAULT_SHORT_DESCRIPTION));
    }
}
