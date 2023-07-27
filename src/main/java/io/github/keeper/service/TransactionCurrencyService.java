package io.github.keeper.service;

import io.github.keeper.service.dto.TransactionCurrencyDTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link io.github.keeper.domain.TransactionCurrency}.
 */
public interface TransactionCurrencyService {
    /**
     * Save a transactionCurrency.
     *
     * @param transactionCurrencyDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<TransactionCurrencyDTO> save(TransactionCurrencyDTO transactionCurrencyDTO);

    /**
     * Updates a transactionCurrency.
     *
     * @param transactionCurrencyDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<TransactionCurrencyDTO> update(TransactionCurrencyDTO transactionCurrencyDTO);

    /**
     * Partially updates a transactionCurrency.
     *
     * @param transactionCurrencyDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<TransactionCurrencyDTO> partialUpdate(TransactionCurrencyDTO transactionCurrencyDTO);

    /**
     * Get all the transactionCurrencies.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<TransactionCurrencyDTO> findAll(Pageable pageable);

    /**
     * Returns the number of transactionCurrencies available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Returns the number of transactionCurrencies available in search repository.
     *
     */
    Mono<Long> searchCount();

    /**
     * Get the "id" transactionCurrency.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<TransactionCurrencyDTO> findOne(Long id);

    /**
     * Delete the "id" transactionCurrency.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(Long id);

    /**
     * Search for the transactionCurrency corresponding to the query.
     *
     * @param query the query of the search.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<TransactionCurrencyDTO> search(String query, Pageable pageable);
}
