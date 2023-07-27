package io.github.keeper.repository;

import io.github.keeper.domain.EventType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the EventType entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EventTypeRepository extends ReactiveCrudRepository<EventType, Long>, EventTypeRepositoryInternal {
    Flux<EventType> findAllBy(Pageable pageable);

    @Override
    <S extends EventType> Mono<S> save(S entity);

    @Override
    Flux<EventType> findAll();

    @Override
    Mono<EventType> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface EventTypeRepositoryInternal {
    <S extends EventType> Mono<S> save(S entity);

    Flux<EventType> findAllBy(Pageable pageable);

    Flux<EventType> findAll();

    Mono<EventType> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<EventType> findAllBy(Pageable pageable, Criteria criteria);

}
