package io.github.keeper.service;

import io.github.keeper.service.dto.DealerTypeDTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link io.github.keeper.domain.DealerType}.
 */
public interface DealerTypeService {
    /**
     * Save a dealerType.
     *
     * @param dealerTypeDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<DealerTypeDTO> save(DealerTypeDTO dealerTypeDTO);

    /**
     * Updates a dealerType.
     *
     * @param dealerTypeDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<DealerTypeDTO> update(DealerTypeDTO dealerTypeDTO);

    /**
     * Partially updates a dealerType.
     *
     * @param dealerTypeDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<DealerTypeDTO> partialUpdate(DealerTypeDTO dealerTypeDTO);

    /**
     * Get all the dealerTypes.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<DealerTypeDTO> findAll(Pageable pageable);

    /**
     * Returns the number of dealerTypes available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Returns the number of dealerTypes available in search repository.
     *
     */
    Mono<Long> searchCount();

    /**
     * Get the "id" dealerType.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<DealerTypeDTO> findOne(Long id);

    /**
     * Delete the "id" dealerType.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(Long id);

    /**
     * Search for the dealerType corresponding to the query.
     *
     * @param query the query of the search.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<DealerTypeDTO> search(String query, Pageable pageable);
}
