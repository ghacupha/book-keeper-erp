package io.github.keeper.repository;

import io.github.keeper.domain.TransactionCurrency;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the TransactionCurrency entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TransactionCurrencyRepository
    extends ReactiveCrudRepository<TransactionCurrency, Long>, TransactionCurrencyRepositoryInternal {
    Flux<TransactionCurrency> findAllBy(Pageable pageable);

    @Override
    <S extends TransactionCurrency> Mono<S> save(S entity);

    @Override
    Flux<TransactionCurrency> findAll();

    @Override
    Mono<TransactionCurrency> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface TransactionCurrencyRepositoryInternal {
    <S extends TransactionCurrency> Mono<S> save(S entity);

    Flux<TransactionCurrency> findAllBy(Pageable pageable);

    Flux<TransactionCurrency> findAll();

    Mono<TransactionCurrency> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<TransactionCurrency> findAllBy(Pageable pageable, Criteria criteria);

}
