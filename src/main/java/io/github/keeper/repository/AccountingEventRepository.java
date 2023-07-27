package io.github.keeper.repository;

import io.github.keeper.domain.AccountingEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the AccountingEvent entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AccountingEventRepository extends ReactiveCrudRepository<AccountingEvent, Long>, AccountingEventRepositoryInternal {
    Flux<AccountingEvent> findAllBy(Pageable pageable);

    @Override
    Mono<AccountingEvent> findOneWithEagerRelationships(Long id);

    @Override
    Flux<AccountingEvent> findAllWithEagerRelationships();

    @Override
    Flux<AccountingEvent> findAllWithEagerRelationships(Pageable page);

    @Query("SELECT * FROM accounting_event entity WHERE entity.event_type_id = :id")
    Flux<AccountingEvent> findByEventType(Long id);

    @Query("SELECT * FROM accounting_event entity WHERE entity.event_type_id IS NULL")
    Flux<AccountingEvent> findAllWhereEventTypeIsNull();

    @Query("SELECT * FROM accounting_event entity WHERE entity.dealer_id = :id")
    Flux<AccountingEvent> findByDealer(Long id);

    @Query("SELECT * FROM accounting_event entity WHERE entity.dealer_id IS NULL")
    Flux<AccountingEvent> findAllWhereDealerIsNull();

    @Override
    <S extends AccountingEvent> Mono<S> save(S entity);

    @Override
    Flux<AccountingEvent> findAll();

    @Override
    Mono<AccountingEvent> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface AccountingEventRepositoryInternal {
    <S extends AccountingEvent> Mono<S> save(S entity);

    Flux<AccountingEvent> findAllBy(Pageable pageable);

    Flux<AccountingEvent> findAll();

    Mono<AccountingEvent> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<AccountingEvent> findAllBy(Pageable pageable, Criteria criteria);

    Mono<AccountingEvent> findOneWithEagerRelationships(Long id);

    Flux<AccountingEvent> findAllWithEagerRelationships();

    Flux<AccountingEvent> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(Long id);
}
