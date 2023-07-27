package io.github.keeper.service;

import io.github.keeper.service.dto.TransactionAccountDTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link io.github.keeper.domain.TransactionAccount}.
 */
public interface TransactionAccountService {
    /**
     * Save a transactionAccount.
     *
     * @param transactionAccountDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<TransactionAccountDTO> save(TransactionAccountDTO transactionAccountDTO);

    /**
     * Updates a transactionAccount.
     *
     * @param transactionAccountDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<TransactionAccountDTO> update(TransactionAccountDTO transactionAccountDTO);

    /**
     * Partially updates a transactionAccount.
     *
     * @param transactionAccountDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<TransactionAccountDTO> partialUpdate(TransactionAccountDTO transactionAccountDTO);

    /**
     * Get all the transactionAccounts.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<TransactionAccountDTO> findAll(Pageable pageable);

    /**
     * Get all the transactionAccounts with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<TransactionAccountDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Returns the number of transactionAccounts available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Returns the number of transactionAccounts available in search repository.
     *
     */
    Mono<Long> searchCount();

    /**
     * Get the "id" transactionAccount.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<TransactionAccountDTO> findOne(Long id);

    /**
     * Delete the "id" transactionAccount.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(Long id);

    /**
     * Search for the transactionAccount corresponding to the query.
     *
     * @param query the query of the search.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<TransactionAccountDTO> search(String query, Pageable pageable);
}
