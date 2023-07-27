package io.github.keeper.service;

import io.github.keeper.service.dto.TransactionAccountTypeDTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link io.github.keeper.domain.TransactionAccountType}.
 */
public interface TransactionAccountTypeService {
    /**
     * Save a transactionAccountType.
     *
     * @param transactionAccountTypeDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<TransactionAccountTypeDTO> save(TransactionAccountTypeDTO transactionAccountTypeDTO);

    /**
     * Updates a transactionAccountType.
     *
     * @param transactionAccountTypeDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<TransactionAccountTypeDTO> update(TransactionAccountTypeDTO transactionAccountTypeDTO);

    /**
     * Partially updates a transactionAccountType.
     *
     * @param transactionAccountTypeDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<TransactionAccountTypeDTO> partialUpdate(TransactionAccountTypeDTO transactionAccountTypeDTO);

    /**
     * Get all the transactionAccountTypes.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<TransactionAccountTypeDTO> findAll(Pageable pageable);

    /**
     * Returns the number of transactionAccountTypes available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Returns the number of transactionAccountTypes available in search repository.
     *
     */
    Mono<Long> searchCount();

    /**
     * Get the "id" transactionAccountType.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<TransactionAccountTypeDTO> findOne(Long id);

    /**
     * Delete the "id" transactionAccountType.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(Long id);

    /**
     * Search for the transactionAccountType corresponding to the query.
     *
     * @param query the query of the search.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<TransactionAccountTypeDTO> search(String query, Pageable pageable);
}
