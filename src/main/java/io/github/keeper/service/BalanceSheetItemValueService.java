package io.github.keeper.service;

import io.github.keeper.service.dto.BalanceSheetItemValueDTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link io.github.keeper.domain.BalanceSheetItemValue}.
 */
public interface BalanceSheetItemValueService {
    /**
     * Save a balanceSheetItemValue.
     *
     * @param balanceSheetItemValueDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<BalanceSheetItemValueDTO> save(BalanceSheetItemValueDTO balanceSheetItemValueDTO);

    /**
     * Updates a balanceSheetItemValue.
     *
     * @param balanceSheetItemValueDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<BalanceSheetItemValueDTO> update(BalanceSheetItemValueDTO balanceSheetItemValueDTO);

    /**
     * Partially updates a balanceSheetItemValue.
     *
     * @param balanceSheetItemValueDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<BalanceSheetItemValueDTO> partialUpdate(BalanceSheetItemValueDTO balanceSheetItemValueDTO);

    /**
     * Get all the balanceSheetItemValues.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<BalanceSheetItemValueDTO> findAll(Pageable pageable);

    /**
     * Get all the balanceSheetItemValues with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<BalanceSheetItemValueDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Returns the number of balanceSheetItemValues available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Returns the number of balanceSheetItemValues available in search repository.
     *
     */
    Mono<Long> searchCount();

    /**
     * Get the "id" balanceSheetItemValue.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<BalanceSheetItemValueDTO> findOne(Long id);

    /**
     * Delete the "id" balanceSheetItemValue.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(Long id);

    /**
     * Search for the balanceSheetItemValue corresponding to the query.
     *
     * @param query the query of the search.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<BalanceSheetItemValueDTO> search(String query, Pageable pageable);
}
