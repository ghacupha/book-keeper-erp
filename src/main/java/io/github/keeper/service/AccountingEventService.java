package io.github.keeper.service;

import io.github.keeper.service.dto.AccountingEventDTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link io.github.keeper.domain.AccountingEvent}.
 */
public interface AccountingEventService {
    /**
     * Save a accountingEvent.
     *
     * @param accountingEventDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<AccountingEventDTO> save(AccountingEventDTO accountingEventDTO);

    /**
     * Updates a accountingEvent.
     *
     * @param accountingEventDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<AccountingEventDTO> update(AccountingEventDTO accountingEventDTO);

    /**
     * Partially updates a accountingEvent.
     *
     * @param accountingEventDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<AccountingEventDTO> partialUpdate(AccountingEventDTO accountingEventDTO);

    /**
     * Get all the accountingEvents.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<AccountingEventDTO> findAll(Pageable pageable);

    /**
     * Get all the accountingEvents with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<AccountingEventDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Returns the number of accountingEvents available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Returns the number of accountingEvents available in search repository.
     *
     */
    Mono<Long> searchCount();

    /**
     * Get the "id" accountingEvent.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<AccountingEventDTO> findOne(Long id);

    /**
     * Delete the "id" accountingEvent.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(Long id);

    /**
     * Search for the accountingEvent corresponding to the query.
     *
     * @param query the query of the search.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<AccountingEventDTO> search(String query, Pageable pageable);
}
