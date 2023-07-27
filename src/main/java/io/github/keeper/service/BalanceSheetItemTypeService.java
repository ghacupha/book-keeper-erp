package io.github.keeper.service;

import io.github.keeper.service.dto.BalanceSheetItemTypeDTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link io.github.keeper.domain.BalanceSheetItemType}.
 */
public interface BalanceSheetItemTypeService {
    /**
     * Save a balanceSheetItemType.
     *
     * @param balanceSheetItemTypeDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<BalanceSheetItemTypeDTO> save(BalanceSheetItemTypeDTO balanceSheetItemTypeDTO);

    /**
     * Updates a balanceSheetItemType.
     *
     * @param balanceSheetItemTypeDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<BalanceSheetItemTypeDTO> update(BalanceSheetItemTypeDTO balanceSheetItemTypeDTO);

    /**
     * Partially updates a balanceSheetItemType.
     *
     * @param balanceSheetItemTypeDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<BalanceSheetItemTypeDTO> partialUpdate(BalanceSheetItemTypeDTO balanceSheetItemTypeDTO);

    /**
     * Get all the balanceSheetItemTypes.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<BalanceSheetItemTypeDTO> findAll(Pageable pageable);

    /**
     * Get all the balanceSheetItemTypes with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<BalanceSheetItemTypeDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Returns the number of balanceSheetItemTypes available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Returns the number of balanceSheetItemTypes available in search repository.
     *
     */
    Mono<Long> searchCount();

    /**
     * Get the "id" balanceSheetItemType.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<BalanceSheetItemTypeDTO> findOne(Long id);

    /**
     * Delete the "id" balanceSheetItemType.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(Long id);

    /**
     * Search for the balanceSheetItemType corresponding to the query.
     *
     * @param query the query of the search.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<BalanceSheetItemTypeDTO> search(String query, Pageable pageable);
}
