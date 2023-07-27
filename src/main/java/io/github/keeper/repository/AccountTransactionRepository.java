package io.github.keeper.repository;

import io.github.keeper.domain.AccountTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the AccountTransaction entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AccountTransactionRepository
    extends ReactiveCrudRepository<AccountTransaction, Long>, AccountTransactionRepositoryInternal {
    Flux<AccountTransaction> findAllBy(Pageable pageable);

    @Override
    <S extends AccountTransaction> Mono<S> save(S entity);

    @Override
    Flux<AccountTransaction> findAll();

    @Override
    Mono<AccountTransaction> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface AccountTransactionRepositoryInternal {
    <S extends AccountTransaction> Mono<S> save(S entity);

    Flux<AccountTransaction> findAllBy(Pageable pageable);

    Flux<AccountTransaction> findAll();

    Mono<AccountTransaction> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<AccountTransaction> findAllBy(Pageable pageable, Criteria criteria);

}
