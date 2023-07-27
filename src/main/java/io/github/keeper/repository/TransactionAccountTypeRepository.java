package io.github.keeper.repository;

import io.github.keeper.domain.TransactionAccountType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the TransactionAccountType entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TransactionAccountTypeRepository
    extends ReactiveCrudRepository<TransactionAccountType, Long>, TransactionAccountTypeRepositoryInternal {
    Flux<TransactionAccountType> findAllBy(Pageable pageable);

    @Override
    <S extends TransactionAccountType> Mono<S> save(S entity);

    @Override
    Flux<TransactionAccountType> findAll();

    @Override
    Mono<TransactionAccountType> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface TransactionAccountTypeRepositoryInternal {
    <S extends TransactionAccountType> Mono<S> save(S entity);

    Flux<TransactionAccountType> findAllBy(Pageable pageable);

    Flux<TransactionAccountType> findAll();

    Mono<TransactionAccountType> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<TransactionAccountType> findAllBy(Pageable pageable, Criteria criteria);

}
