package io.github.keeper.repository;

import io.github.keeper.domain.DealerType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the DealerType entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DealerTypeRepository extends ReactiveCrudRepository<DealerType, Long>, DealerTypeRepositoryInternal {
    Flux<DealerType> findAllBy(Pageable pageable);

    @Override
    <S extends DealerType> Mono<S> save(S entity);

    @Override
    Flux<DealerType> findAll();

    @Override
    Mono<DealerType> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface DealerTypeRepositoryInternal {
    <S extends DealerType> Mono<S> save(S entity);

    Flux<DealerType> findAllBy(Pageable pageable);

    Flux<DealerType> findAll();

    Mono<DealerType> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<DealerType> findAllBy(Pageable pageable, Criteria criteria);

}
