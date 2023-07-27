package io.github.keeper.service;

import io.github.keeper.service.dto.DealerDTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link io.github.keeper.domain.Dealer}.
 */
public interface DealerService {
    /**
     * Save a dealer.
     *
     * @param dealerDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<DealerDTO> save(DealerDTO dealerDTO);

    /**
     * Updates a dealer.
     *
     * @param dealerDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<DealerDTO> update(DealerDTO dealerDTO);

    /**
     * Partially updates a dealer.
     *
     * @param dealerDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<DealerDTO> partialUpdate(DealerDTO dealerDTO);

    /**
     * Get all the dealers.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<DealerDTO> findAll(Pageable pageable);

    /**
     * Get all the dealers with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<DealerDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Returns the number of dealers available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Returns the number of dealers available in search repository.
     *
     */
    Mono<Long> searchCount();

    /**
     * Get the "id" dealer.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<DealerDTO> findOne(Long id);

    /**
     * Delete the "id" dealer.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(Long id);

    /**
     * Search for the dealer corresponding to the query.
     *
     * @param query the query of the search.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<DealerDTO> search(String query, Pageable pageable);
}
