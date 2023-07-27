package io.github.keeper.service;

import io.github.keeper.service.dto.EventTypeDTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link io.github.keeper.domain.EventType}.
 */
public interface EventTypeService {
    /**
     * Save a eventType.
     *
     * @param eventTypeDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<EventTypeDTO> save(EventTypeDTO eventTypeDTO);

    /**
     * Updates a eventType.
     *
     * @param eventTypeDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<EventTypeDTO> update(EventTypeDTO eventTypeDTO);

    /**
     * Partially updates a eventType.
     *
     * @param eventTypeDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<EventTypeDTO> partialUpdate(EventTypeDTO eventTypeDTO);

    /**
     * Get all the eventTypes.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<EventTypeDTO> findAll(Pageable pageable);

    /**
     * Returns the number of eventTypes available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Returns the number of eventTypes available in search repository.
     *
     */
    Mono<Long> searchCount();

    /**
     * Get the "id" eventType.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<EventTypeDTO> findOne(Long id);

    /**
     * Delete the "id" eventType.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(Long id);

    /**
     * Search for the eventType corresponding to the query.
     *
     * @param query the query of the search.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<EventTypeDTO> search(String query, Pageable pageable);
}
