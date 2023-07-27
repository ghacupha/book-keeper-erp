package io.github.keeper.repository;

import io.github.keeper.domain.Dealer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Dealer entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DealerRepository extends ReactiveCrudRepository<Dealer, Long>, DealerRepositoryInternal {
    Flux<Dealer> findAllBy(Pageable pageable);

    @Override
    Mono<Dealer> findOneWithEagerRelationships(Long id);

    @Override
    Flux<Dealer> findAllWithEagerRelationships();

    @Override
    Flux<Dealer> findAllWithEagerRelationships(Pageable page);

    @Query("SELECT * FROM dealer entity WHERE entity.dealer_type_id = :id")
    Flux<Dealer> findByDealerType(Long id);

    @Query("SELECT * FROM dealer entity WHERE entity.dealer_type_id IS NULL")
    Flux<Dealer> findAllWhereDealerTypeIsNull();

    @Override
    <S extends Dealer> Mono<S> save(S entity);

    @Override
    Flux<Dealer> findAll();

    @Override
    Mono<Dealer> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface DealerRepositoryInternal {
    <S extends Dealer> Mono<S> save(S entity);

    Flux<Dealer> findAllBy(Pageable pageable);

    Flux<Dealer> findAll();

    Mono<Dealer> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Dealer> findAllBy(Pageable pageable, Criteria criteria);

    Mono<Dealer> findOneWithEagerRelationships(Long id);

    Flux<Dealer> findAllWithEagerRelationships();

    Flux<Dealer> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(Long id);
}
