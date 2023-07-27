package io.github.keeper.service;

import io.github.keeper.service.dto.AccountTransactionDTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link io.github.keeper.domain.AccountTransaction}.
 */
public interface AccountTransactionService {
    /**
     * Save a accountTransaction.
     *
     * @param accountTransactionDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<AccountTransactionDTO> save(AccountTransactionDTO accountTransactionDTO);

    /**
     * Updates a accountTransaction.
     *
     * @param accountTransactionDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<AccountTransactionDTO> update(AccountTransactionDTO accountTransactionDTO);

    /**
     * Partially updates a accountTransaction.
     *
     * @param accountTransactionDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<AccountTransactionDTO> partialUpdate(AccountTransactionDTO accountTransactionDTO);

    /**
     * Get all the accountTransactions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<AccountTransactionDTO> findAll(Pageable pageable);

    /**
     * Returns the number of accountTransactions available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Returns the number of accountTransactions available in search repository.
     *
     */
    Mono<Long> searchCount();

    /**
     * Get the "id" accountTransaction.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<AccountTransactionDTO> findOne(Long id);

    /**
     * Delete the "id" accountTransaction.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(Long id);

    /**
     * Search for the accountTransaction corresponding to the query.
     *
     * @param query the query of the search.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<AccountTransactionDTO> search(String query, Pageable pageable);
}
