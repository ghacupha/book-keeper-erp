package io.github.keeper.service;

import io.github.keeper.service.dto.TransactionEntryDTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link io.github.keeper.domain.TransactionEntry}.
 */
public interface TransactionEntryService {
    /**
     * Save a transactionEntry.
     *
     * @param transactionEntryDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<TransactionEntryDTO> save(TransactionEntryDTO transactionEntryDTO);

    /**
     * Updates a transactionEntry.
     *
     * @param transactionEntryDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<TransactionEntryDTO> update(TransactionEntryDTO transactionEntryDTO);

    /**
     * Partially updates a transactionEntry.
     *
     * @param transactionEntryDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<TransactionEntryDTO> partialUpdate(TransactionEntryDTO transactionEntryDTO);

    /**
     * Get all the transactionEntries.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<TransactionEntryDTO> findAll(Pageable pageable);

    /**
     * Get all the transactionEntries with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<TransactionEntryDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Returns the number of transactionEntries available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Returns the number of transactionEntries available in search repository.
     *
     */
    Mono<Long> searchCount();

    /**
     * Get the "id" transactionEntry.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<TransactionEntryDTO> findOne(Long id);

    /**
     * Delete the "id" transactionEntry.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(Long id);

    /**
     * Search for the transactionEntry corresponding to the query.
     *
     * @param query the query of the search.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<TransactionEntryDTO> search(String query, Pageable pageable);
}
