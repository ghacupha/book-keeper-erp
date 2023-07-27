package io.github.keeper.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import io.github.keeper.IntegrationTest;
import io.github.keeper.domain.EventType;
import io.github.keeper.repository.EntityManager;
import io.github.keeper.repository.EventTypeRepository;
import io.github.keeper.repository.search.EventTypeSearchRepository;
import io.github.keeper.service.dto.EventTypeDTO;
import io.github.keeper.service.mapper.EventTypeMapper;
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
 * Integration tests for the {@link EventTypeResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class EventTypeResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/event-types";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/event-types";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private EventTypeRepository eventTypeRepository;

    @Autowired
    private EventTypeMapper eventTypeMapper;

    @Autowired
    private EventTypeSearchRepository eventTypeSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private EventType eventType;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static EventType createEntity(EntityManager em) {
        EventType eventType = new EventType().name(DEFAULT_NAME);
        return eventType;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static EventType createUpdatedEntity(EntityManager em) {
        EventType eventType = new EventType().name(UPDATED_NAME);
        return eventType;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(EventType.class).block();
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
        eventTypeSearchRepository.deleteAll().block();
        assertThat(eventTypeSearchRepository.count().block()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        eventType = createEntity(em);
    }

    @Test
    void createEventType() throws Exception {
        int databaseSizeBeforeCreate = eventTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(eventTypeSearchRepository.findAll().collectList().block());
        // Create the EventType
        EventTypeDTO eventTypeDTO = eventTypeMapper.toDto(eventType);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(eventTypeDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the EventType in the database
        List<EventType> eventTypeList = eventTypeRepository.findAll().collectList().block();
        assertThat(eventTypeList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(eventTypeSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        EventType testEventType = eventTypeList.get(eventTypeList.size() - 1);
        assertThat(testEventType.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    void createEventTypeWithExistingId() throws Exception {
        // Create the EventType with an existing ID
        eventType.setId(1L);
        EventTypeDTO eventTypeDTO = eventTypeMapper.toDto(eventType);

        int databaseSizeBeforeCreate = eventTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(eventTypeSearchRepository.findAll().collectList().block());

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(eventTypeDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the EventType in the database
        List<EventType> eventTypeList = eventTypeRepository.findAll().collectList().block();
        assertThat(eventTypeList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(eventTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = eventTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(eventTypeSearchRepository.findAll().collectList().block());
        // set the field null
        eventType.setName(null);

        // Create the EventType, which fails.
        EventTypeDTO eventTypeDTO = eventTypeMapper.toDto(eventType);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(eventTypeDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<EventType> eventTypeList = eventTypeRepository.findAll().collectList().block();
        assertThat(eventTypeList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(eventTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void getAllEventTypes() {
        // Initialize the database
        eventTypeRepository.save(eventType).block();

        // Get all the eventTypeList
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
            .value(hasItem(eventType.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME));
    }

    @Test
    void getEventType() {
        // Initialize the database
        eventTypeRepository.save(eventType).block();

        // Get the eventType
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, eventType.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(eventType.getId().intValue()))
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME));
    }

    @Test
    void getNonExistingEventType() {
        // Get the eventType
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingEventType() throws Exception {
        // Initialize the database
        eventTypeRepository.save(eventType).block();

        int databaseSizeBeforeUpdate = eventTypeRepository.findAll().collectList().block().size();
        eventTypeSearchRepository.save(eventType).block();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(eventTypeSearchRepository.findAll().collectList().block());

        // Update the eventType
        EventType updatedEventType = eventTypeRepository.findById(eventType.getId()).block();
        updatedEventType.name(UPDATED_NAME);
        EventTypeDTO eventTypeDTO = eventTypeMapper.toDto(updatedEventType);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, eventTypeDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(eventTypeDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the EventType in the database
        List<EventType> eventTypeList = eventTypeRepository.findAll().collectList().block();
        assertThat(eventTypeList).hasSize(databaseSizeBeforeUpdate);
        EventType testEventType = eventTypeList.get(eventTypeList.size() - 1);
        assertThat(testEventType.getName()).isEqualTo(UPDATED_NAME);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(eventTypeSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<EventType> eventTypeSearchList = IterableUtils.toList(eventTypeSearchRepository.findAll().collectList().block());
                EventType testEventTypeSearch = eventTypeSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testEventTypeSearch.getName()).isEqualTo(UPDATED_NAME);
            });
    }

    @Test
    void putNonExistingEventType() throws Exception {
        int databaseSizeBeforeUpdate = eventTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(eventTypeSearchRepository.findAll().collectList().block());
        eventType.setId(count.incrementAndGet());

        // Create the EventType
        EventTypeDTO eventTypeDTO = eventTypeMapper.toDto(eventType);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, eventTypeDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(eventTypeDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the EventType in the database
        List<EventType> eventTypeList = eventTypeRepository.findAll().collectList().block();
        assertThat(eventTypeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(eventTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithIdMismatchEventType() throws Exception {
        int databaseSizeBeforeUpdate = eventTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(eventTypeSearchRepository.findAll().collectList().block());
        eventType.setId(count.incrementAndGet());

        // Create the EventType
        EventTypeDTO eventTypeDTO = eventTypeMapper.toDto(eventType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(eventTypeDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the EventType in the database
        List<EventType> eventTypeList = eventTypeRepository.findAll().collectList().block();
        assertThat(eventTypeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(eventTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithMissingIdPathParamEventType() throws Exception {
        int databaseSizeBeforeUpdate = eventTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(eventTypeSearchRepository.findAll().collectList().block());
        eventType.setId(count.incrementAndGet());

        // Create the EventType
        EventTypeDTO eventTypeDTO = eventTypeMapper.toDto(eventType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(eventTypeDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the EventType in the database
        List<EventType> eventTypeList = eventTypeRepository.findAll().collectList().block();
        assertThat(eventTypeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(eventTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void partialUpdateEventTypeWithPatch() throws Exception {
        // Initialize the database
        eventTypeRepository.save(eventType).block();

        int databaseSizeBeforeUpdate = eventTypeRepository.findAll().collectList().block().size();

        // Update the eventType using partial update
        EventType partialUpdatedEventType = new EventType();
        partialUpdatedEventType.setId(eventType.getId());

        partialUpdatedEventType.name(UPDATED_NAME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedEventType.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedEventType))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the EventType in the database
        List<EventType> eventTypeList = eventTypeRepository.findAll().collectList().block();
        assertThat(eventTypeList).hasSize(databaseSizeBeforeUpdate);
        EventType testEventType = eventTypeList.get(eventTypeList.size() - 1);
        assertThat(testEventType.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    void fullUpdateEventTypeWithPatch() throws Exception {
        // Initialize the database
        eventTypeRepository.save(eventType).block();

        int databaseSizeBeforeUpdate = eventTypeRepository.findAll().collectList().block().size();

        // Update the eventType using partial update
        EventType partialUpdatedEventType = new EventType();
        partialUpdatedEventType.setId(eventType.getId());

        partialUpdatedEventType.name(UPDATED_NAME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedEventType.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedEventType))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the EventType in the database
        List<EventType> eventTypeList = eventTypeRepository.findAll().collectList().block();
        assertThat(eventTypeList).hasSize(databaseSizeBeforeUpdate);
        EventType testEventType = eventTypeList.get(eventTypeList.size() - 1);
        assertThat(testEventType.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    void patchNonExistingEventType() throws Exception {
        int databaseSizeBeforeUpdate = eventTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(eventTypeSearchRepository.findAll().collectList().block());
        eventType.setId(count.incrementAndGet());

        // Create the EventType
        EventTypeDTO eventTypeDTO = eventTypeMapper.toDto(eventType);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, eventTypeDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(eventTypeDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the EventType in the database
        List<EventType> eventTypeList = eventTypeRepository.findAll().collectList().block();
        assertThat(eventTypeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(eventTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithIdMismatchEventType() throws Exception {
        int databaseSizeBeforeUpdate = eventTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(eventTypeSearchRepository.findAll().collectList().block());
        eventType.setId(count.incrementAndGet());

        // Create the EventType
        EventTypeDTO eventTypeDTO = eventTypeMapper.toDto(eventType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(eventTypeDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the EventType in the database
        List<EventType> eventTypeList = eventTypeRepository.findAll().collectList().block();
        assertThat(eventTypeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(eventTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithMissingIdPathParamEventType() throws Exception {
        int databaseSizeBeforeUpdate = eventTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(eventTypeSearchRepository.findAll().collectList().block());
        eventType.setId(count.incrementAndGet());

        // Create the EventType
        EventTypeDTO eventTypeDTO = eventTypeMapper.toDto(eventType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(eventTypeDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the EventType in the database
        List<EventType> eventTypeList = eventTypeRepository.findAll().collectList().block();
        assertThat(eventTypeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(eventTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void deleteEventType() {
        // Initialize the database
        eventTypeRepository.save(eventType).block();
        eventTypeRepository.save(eventType).block();
        eventTypeSearchRepository.save(eventType).block();

        int databaseSizeBeforeDelete = eventTypeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(eventTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the eventType
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, eventType.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<EventType> eventTypeList = eventTypeRepository.findAll().collectList().block();
        assertThat(eventTypeList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(eventTypeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    void searchEventType() {
        // Initialize the database
        eventType = eventTypeRepository.save(eventType).block();
        eventTypeSearchRepository.save(eventType).block();

        // Search the eventType
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + eventType.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(eventType.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME));
    }
}
