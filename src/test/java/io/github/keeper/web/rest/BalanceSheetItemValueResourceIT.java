package io.github.keeper.web.rest;

import static io.github.keeper.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import io.github.keeper.IntegrationTest;
import io.github.keeper.domain.BalanceSheetItemType;
import io.github.keeper.domain.BalanceSheetItemValue;
import io.github.keeper.repository.BalanceSheetItemValueRepository;
import io.github.keeper.repository.EntityManager;
import io.github.keeper.repository.search.BalanceSheetItemValueSearchRepository;
import io.github.keeper.service.BalanceSheetItemValueService;
import io.github.keeper.service.dto.BalanceSheetItemValueDTO;
import io.github.keeper.service.mapper.BalanceSheetItemValueMapper;
import java.math.BigDecimal;
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
 * Integration tests for the {@link BalanceSheetItemValueResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class BalanceSheetItemValueResourceIT {

    private static final String DEFAULT_SHORT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_SHORT_DESCRIPTION = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_EFFECTIVE_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_EFFECTIVE_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final BigDecimal DEFAULT_ITEM_AMOUNT = new BigDecimal(1);
    private static final BigDecimal UPDATED_ITEM_AMOUNT = new BigDecimal(2);

    private static final String ENTITY_API_URL = "/api/balance-sheet-item-values";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/balance-sheet-item-values";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private BalanceSheetItemValueRepository balanceSheetItemValueRepository;

    @Mock
    private BalanceSheetItemValueRepository balanceSheetItemValueRepositoryMock;

    @Autowired
    private BalanceSheetItemValueMapper balanceSheetItemValueMapper;

    @Mock
    private BalanceSheetItemValueService balanceSheetItemValueServiceMock;

    @Autowired
    private BalanceSheetItemValueSearchRepository balanceSheetItemValueSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private BalanceSheetItemValue balanceSheetItemValue;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BalanceSheetItemValue createEntity(EntityManager em) {
        BalanceSheetItemValue balanceSheetItemValue = new BalanceSheetItemValue()
            .shortDescription(DEFAULT_SHORT_DESCRIPTION)
            .effectiveDate(DEFAULT_EFFECTIVE_DATE)
            .itemAmount(DEFAULT_ITEM_AMOUNT);
        // Add required entity
        BalanceSheetItemType balanceSheetItemType;
        balanceSheetItemType = em.insert(BalanceSheetItemTypeResourceIT.createEntity(em)).block();
        balanceSheetItemValue.setItemType(balanceSheetItemType);
        return balanceSheetItemValue;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BalanceSheetItemValue createUpdatedEntity(EntityManager em) {
        BalanceSheetItemValue balanceSheetItemValue = new BalanceSheetItemValue()
            .shortDescription(UPDATED_SHORT_DESCRIPTION)
            .effectiveDate(UPDATED_EFFECTIVE_DATE)
            .itemAmount(UPDATED_ITEM_AMOUNT);
        // Add required entity
        BalanceSheetItemType balanceSheetItemType;
        balanceSheetItemType = em.insert(BalanceSheetItemTypeResourceIT.createUpdatedEntity(em)).block();
        balanceSheetItemValue.setItemType(balanceSheetItemType);
        return balanceSheetItemValue;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(BalanceSheetItemValue.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
        BalanceSheetItemTypeResourceIT.deleteEntities(em);
    }

    @AfterEach
    public void cleanup() {
        deleteEntities(em);
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        balanceSheetItemValueSearchRepository.deleteAll().block();
        assertThat(balanceSheetItemValueSearchRepository.count().block()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        balanceSheetItemValue = createEntity(em);
    }

    @Test
    void createBalanceSheetItemValue() throws Exception {
        int databaseSizeBeforeCreate = balanceSheetItemValueRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(balanceSheetItemValueSearchRepository.findAll().collectList().block());
        // Create the BalanceSheetItemValue
        BalanceSheetItemValueDTO balanceSheetItemValueDTO = balanceSheetItemValueMapper.toDto(balanceSheetItemValue);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(balanceSheetItemValueDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the BalanceSheetItemValue in the database
        List<BalanceSheetItemValue> balanceSheetItemValueList = balanceSheetItemValueRepository.findAll().collectList().block();
        assertThat(balanceSheetItemValueList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(balanceSheetItemValueSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        BalanceSheetItemValue testBalanceSheetItemValue = balanceSheetItemValueList.get(balanceSheetItemValueList.size() - 1);
        assertThat(testBalanceSheetItemValue.getShortDescription()).isEqualTo(DEFAULT_SHORT_DESCRIPTION);
        assertThat(testBalanceSheetItemValue.getEffectiveDate()).isEqualTo(DEFAULT_EFFECTIVE_DATE);
        assertThat(testBalanceSheetItemValue.getItemAmount()).isEqualByComparingTo(DEFAULT_ITEM_AMOUNT);
    }

    @Test
    void createBalanceSheetItemValueWithExistingId() throws Exception {
        // Create the BalanceSheetItemValue with an existing ID
        balanceSheetItemValue.setId(1L);
        BalanceSheetItemValueDTO balanceSheetItemValueDTO = balanceSheetItemValueMapper.toDto(balanceSheetItemValue);

        int databaseSizeBeforeCreate = balanceSheetItemValueRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(balanceSheetItemValueSearchRepository.findAll().collectList().block());

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(balanceSheetItemValueDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the BalanceSheetItemValue in the database
        List<BalanceSheetItemValue> balanceSheetItemValueList = balanceSheetItemValueRepository.findAll().collectList().block();
        assertThat(balanceSheetItemValueList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(balanceSheetItemValueSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkEffectiveDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = balanceSheetItemValueRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(balanceSheetItemValueSearchRepository.findAll().collectList().block());
        // set the field null
        balanceSheetItemValue.setEffectiveDate(null);

        // Create the BalanceSheetItemValue, which fails.
        BalanceSheetItemValueDTO balanceSheetItemValueDTO = balanceSheetItemValueMapper.toDto(balanceSheetItemValue);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(balanceSheetItemValueDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<BalanceSheetItemValue> balanceSheetItemValueList = balanceSheetItemValueRepository.findAll().collectList().block();
        assertThat(balanceSheetItemValueList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(balanceSheetItemValueSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkItemAmountIsRequired() throws Exception {
        int databaseSizeBeforeTest = balanceSheetItemValueRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(balanceSheetItemValueSearchRepository.findAll().collectList().block());
        // set the field null
        balanceSheetItemValue.setItemAmount(null);

        // Create the BalanceSheetItemValue, which fails.
        BalanceSheetItemValueDTO balanceSheetItemValueDTO = balanceSheetItemValueMapper.toDto(balanceSheetItemValue);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(balanceSheetItemValueDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<BalanceSheetItemValue> balanceSheetItemValueList = balanceSheetItemValueRepository.findAll().collectList().block();
        assertThat(balanceSheetItemValueList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(balanceSheetItemValueSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void getAllBalanceSheetItemValues() {
        // Initialize the database
        balanceSheetItemValueRepository.save(balanceSheetItemValue).block();

        // Get all the balanceSheetItemValueList
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
            .value(hasItem(balanceSheetItemValue.getId().intValue()))
            .jsonPath("$.[*].shortDescription")
            .value(hasItem(DEFAULT_SHORT_DESCRIPTION))
            .jsonPath("$.[*].effectiveDate")
            .value(hasItem(DEFAULT_EFFECTIVE_DATE.toString()))
            .jsonPath("$.[*].itemAmount")
            .value(hasItem(sameNumber(DEFAULT_ITEM_AMOUNT)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllBalanceSheetItemValuesWithEagerRelationshipsIsEnabled() {
        when(balanceSheetItemValueServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(balanceSheetItemValueServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllBalanceSheetItemValuesWithEagerRelationshipsIsNotEnabled() {
        when(balanceSheetItemValueServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=false").exchange().expectStatus().isOk();
        verify(balanceSheetItemValueRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    void getBalanceSheetItemValue() {
        // Initialize the database
        balanceSheetItemValueRepository.save(balanceSheetItemValue).block();

        // Get the balanceSheetItemValue
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, balanceSheetItemValue.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(balanceSheetItemValue.getId().intValue()))
            .jsonPath("$.shortDescription")
            .value(is(DEFAULT_SHORT_DESCRIPTION))
            .jsonPath("$.effectiveDate")
            .value(is(DEFAULT_EFFECTIVE_DATE.toString()))
            .jsonPath("$.itemAmount")
            .value(is(sameNumber(DEFAULT_ITEM_AMOUNT)));
    }

    @Test
    void getNonExistingBalanceSheetItemValue() {
        // Get the balanceSheetItemValue
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingBalanceSheetItemValue() throws Exception {
        // Initialize the database
        balanceSheetItemValueRepository.save(balanceSheetItemValue).block();

        int databaseSizeBeforeUpdate = balanceSheetItemValueRepository.findAll().collectList().block().size();
        balanceSheetItemValueSearchRepository.save(balanceSheetItemValue).block();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(balanceSheetItemValueSearchRepository.findAll().collectList().block());

        // Update the balanceSheetItemValue
        BalanceSheetItemValue updatedBalanceSheetItemValue = balanceSheetItemValueRepository
            .findById(balanceSheetItemValue.getId())
            .block();
        updatedBalanceSheetItemValue
            .shortDescription(UPDATED_SHORT_DESCRIPTION)
            .effectiveDate(UPDATED_EFFECTIVE_DATE)
            .itemAmount(UPDATED_ITEM_AMOUNT);
        BalanceSheetItemValueDTO balanceSheetItemValueDTO = balanceSheetItemValueMapper.toDto(updatedBalanceSheetItemValue);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, balanceSheetItemValueDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(balanceSheetItemValueDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the BalanceSheetItemValue in the database
        List<BalanceSheetItemValue> balanceSheetItemValueList = balanceSheetItemValueRepository.findAll().collectList().block();
        assertThat(balanceSheetItemValueList).hasSize(databaseSizeBeforeUpdate);
        BalanceSheetItemValue testBalanceSheetItemValue = balanceSheetItemValueList.get(balanceSheetItemValueList.size() - 1);
        assertThat(testBalanceSheetItemValue.getShortDescription()).isEqualTo(UPDATED_SHORT_DESCRIPTION);
        assertThat(testBalanceSheetItemValue.getEffectiveDate()).isEqualTo(UPDATED_EFFECTIVE_DATE);
        assertThat(testBalanceSheetItemValue.getItemAmount()).isEqualByComparingTo(UPDATED_ITEM_AMOUNT);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(balanceSheetItemValueSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<BalanceSheetItemValue> balanceSheetItemValueSearchList = IterableUtils.toList(
                    balanceSheetItemValueSearchRepository.findAll().collectList().block()
                );
                BalanceSheetItemValue testBalanceSheetItemValueSearch = balanceSheetItemValueSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testBalanceSheetItemValueSearch.getShortDescription()).isEqualTo(UPDATED_SHORT_DESCRIPTION);
                assertThat(testBalanceSheetItemValueSearch.getEffectiveDate()).isEqualTo(UPDATED_EFFECTIVE_DATE);
                assertThat(testBalanceSheetItemValueSearch.getItemAmount()).isEqualByComparingTo(UPDATED_ITEM_AMOUNT);
            });
    }

    @Test
    void putNonExistingBalanceSheetItemValue() throws Exception {
        int databaseSizeBeforeUpdate = balanceSheetItemValueRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(balanceSheetItemValueSearchRepository.findAll().collectList().block());
        balanceSheetItemValue.setId(count.incrementAndGet());

        // Create the BalanceSheetItemValue
        BalanceSheetItemValueDTO balanceSheetItemValueDTO = balanceSheetItemValueMapper.toDto(balanceSheetItemValue);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, balanceSheetItemValueDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(balanceSheetItemValueDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the BalanceSheetItemValue in the database
        List<BalanceSheetItemValue> balanceSheetItemValueList = balanceSheetItemValueRepository.findAll().collectList().block();
        assertThat(balanceSheetItemValueList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(balanceSheetItemValueSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithIdMismatchBalanceSheetItemValue() throws Exception {
        int databaseSizeBeforeUpdate = balanceSheetItemValueRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(balanceSheetItemValueSearchRepository.findAll().collectList().block());
        balanceSheetItemValue.setId(count.incrementAndGet());

        // Create the BalanceSheetItemValue
        BalanceSheetItemValueDTO balanceSheetItemValueDTO = balanceSheetItemValueMapper.toDto(balanceSheetItemValue);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(balanceSheetItemValueDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the BalanceSheetItemValue in the database
        List<BalanceSheetItemValue> balanceSheetItemValueList = balanceSheetItemValueRepository.findAll().collectList().block();
        assertThat(balanceSheetItemValueList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(balanceSheetItemValueSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithMissingIdPathParamBalanceSheetItemValue() throws Exception {
        int databaseSizeBeforeUpdate = balanceSheetItemValueRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(balanceSheetItemValueSearchRepository.findAll().collectList().block());
        balanceSheetItemValue.setId(count.incrementAndGet());

        // Create the BalanceSheetItemValue
        BalanceSheetItemValueDTO balanceSheetItemValueDTO = balanceSheetItemValueMapper.toDto(balanceSheetItemValue);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(balanceSheetItemValueDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the BalanceSheetItemValue in the database
        List<BalanceSheetItemValue> balanceSheetItemValueList = balanceSheetItemValueRepository.findAll().collectList().block();
        assertThat(balanceSheetItemValueList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(balanceSheetItemValueSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void partialUpdateBalanceSheetItemValueWithPatch() throws Exception {
        // Initialize the database
        balanceSheetItemValueRepository.save(balanceSheetItemValue).block();

        int databaseSizeBeforeUpdate = balanceSheetItemValueRepository.findAll().collectList().block().size();

        // Update the balanceSheetItemValue using partial update
        BalanceSheetItemValue partialUpdatedBalanceSheetItemValue = new BalanceSheetItemValue();
        partialUpdatedBalanceSheetItemValue.setId(balanceSheetItemValue.getId());

        partialUpdatedBalanceSheetItemValue.shortDescription(UPDATED_SHORT_DESCRIPTION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedBalanceSheetItemValue.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedBalanceSheetItemValue))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the BalanceSheetItemValue in the database
        List<BalanceSheetItemValue> balanceSheetItemValueList = balanceSheetItemValueRepository.findAll().collectList().block();
        assertThat(balanceSheetItemValueList).hasSize(databaseSizeBeforeUpdate);
        BalanceSheetItemValue testBalanceSheetItemValue = balanceSheetItemValueList.get(balanceSheetItemValueList.size() - 1);
        assertThat(testBalanceSheetItemValue.getShortDescription()).isEqualTo(UPDATED_SHORT_DESCRIPTION);
        assertThat(testBalanceSheetItemValue.getEffectiveDate()).isEqualTo(DEFAULT_EFFECTIVE_DATE);
        assertThat(testBalanceSheetItemValue.getItemAmount()).isEqualByComparingTo(DEFAULT_ITEM_AMOUNT);
    }

    @Test
    void fullUpdateBalanceSheetItemValueWithPatch() throws Exception {
        // Initialize the database
        balanceSheetItemValueRepository.save(balanceSheetItemValue).block();

        int databaseSizeBeforeUpdate = balanceSheetItemValueRepository.findAll().collectList().block().size();

        // Update the balanceSheetItemValue using partial update
        BalanceSheetItemValue partialUpdatedBalanceSheetItemValue = new BalanceSheetItemValue();
        partialUpdatedBalanceSheetItemValue.setId(balanceSheetItemValue.getId());

        partialUpdatedBalanceSheetItemValue
            .shortDescription(UPDATED_SHORT_DESCRIPTION)
            .effectiveDate(UPDATED_EFFECTIVE_DATE)
            .itemAmount(UPDATED_ITEM_AMOUNT);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedBalanceSheetItemValue.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedBalanceSheetItemValue))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the BalanceSheetItemValue in the database
        List<BalanceSheetItemValue> balanceSheetItemValueList = balanceSheetItemValueRepository.findAll().collectList().block();
        assertThat(balanceSheetItemValueList).hasSize(databaseSizeBeforeUpdate);
        BalanceSheetItemValue testBalanceSheetItemValue = balanceSheetItemValueList.get(balanceSheetItemValueList.size() - 1);
        assertThat(testBalanceSheetItemValue.getShortDescription()).isEqualTo(UPDATED_SHORT_DESCRIPTION);
        assertThat(testBalanceSheetItemValue.getEffectiveDate()).isEqualTo(UPDATED_EFFECTIVE_DATE);
        assertThat(testBalanceSheetItemValue.getItemAmount()).isEqualByComparingTo(UPDATED_ITEM_AMOUNT);
    }

    @Test
    void patchNonExistingBalanceSheetItemValue() throws Exception {
        int databaseSizeBeforeUpdate = balanceSheetItemValueRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(balanceSheetItemValueSearchRepository.findAll().collectList().block());
        balanceSheetItemValue.setId(count.incrementAndGet());

        // Create the BalanceSheetItemValue
        BalanceSheetItemValueDTO balanceSheetItemValueDTO = balanceSheetItemValueMapper.toDto(balanceSheetItemValue);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, balanceSheetItemValueDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(balanceSheetItemValueDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the BalanceSheetItemValue in the database
        List<BalanceSheetItemValue> balanceSheetItemValueList = balanceSheetItemValueRepository.findAll().collectList().block();
        assertThat(balanceSheetItemValueList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(balanceSheetItemValueSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithIdMismatchBalanceSheetItemValue() throws Exception {
        int databaseSizeBeforeUpdate = balanceSheetItemValueRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(balanceSheetItemValueSearchRepository.findAll().collectList().block());
        balanceSheetItemValue.setId(count.incrementAndGet());

        // Create the BalanceSheetItemValue
        BalanceSheetItemValueDTO balanceSheetItemValueDTO = balanceSheetItemValueMapper.toDto(balanceSheetItemValue);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(balanceSheetItemValueDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the BalanceSheetItemValue in the database
        List<BalanceSheetItemValue> balanceSheetItemValueList = balanceSheetItemValueRepository.findAll().collectList().block();
        assertThat(balanceSheetItemValueList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(balanceSheetItemValueSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithMissingIdPathParamBalanceSheetItemValue() throws Exception {
        int databaseSizeBeforeUpdate = balanceSheetItemValueRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(balanceSheetItemValueSearchRepository.findAll().collectList().block());
        balanceSheetItemValue.setId(count.incrementAndGet());

        // Create the BalanceSheetItemValue
        BalanceSheetItemValueDTO balanceSheetItemValueDTO = balanceSheetItemValueMapper.toDto(balanceSheetItemValue);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(balanceSheetItemValueDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the BalanceSheetItemValue in the database
        List<BalanceSheetItemValue> balanceSheetItemValueList = balanceSheetItemValueRepository.findAll().collectList().block();
        assertThat(balanceSheetItemValueList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(balanceSheetItemValueSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void deleteBalanceSheetItemValue() {
        // Initialize the database
        balanceSheetItemValueRepository.save(balanceSheetItemValue).block();
        balanceSheetItemValueRepository.save(balanceSheetItemValue).block();
        balanceSheetItemValueSearchRepository.save(balanceSheetItemValue).block();

        int databaseSizeBeforeDelete = balanceSheetItemValueRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(balanceSheetItemValueSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the balanceSheetItemValue
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, balanceSheetItemValue.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<BalanceSheetItemValue> balanceSheetItemValueList = balanceSheetItemValueRepository.findAll().collectList().block();
        assertThat(balanceSheetItemValueList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(balanceSheetItemValueSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    void searchBalanceSheetItemValue() {
        // Initialize the database
        balanceSheetItemValue = balanceSheetItemValueRepository.save(balanceSheetItemValue).block();
        balanceSheetItemValueSearchRepository.save(balanceSheetItemValue).block();

        // Search the balanceSheetItemValue
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + balanceSheetItemValue.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(balanceSheetItemValue.getId().intValue()))
            .jsonPath("$.[*].shortDescription")
            .value(hasItem(DEFAULT_SHORT_DESCRIPTION))
            .jsonPath("$.[*].effectiveDate")
            .value(hasItem(DEFAULT_EFFECTIVE_DATE.toString()))
            .jsonPath("$.[*].itemAmount")
            .value(hasItem(sameNumber(DEFAULT_ITEM_AMOUNT)));
    }
}
