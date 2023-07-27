package io.github.keeper.repository;

import io.github.keeper.domain.TransactionAccount;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the TransactionAccount entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TransactionAccountRepository
    extends ReactiveCrudRepository<TransactionAccount, Long>, TransactionAccountRepositoryInternal {
    Flux<TransactionAccount> findAllBy(Pageable pageable);

    @Override
    Mono<TransactionAccount> findOneWithEagerRelationships(Long id);

    @Override
    Flux<TransactionAccount> findAllWithEagerRelationships();

    @Override
    Flux<TransactionAccount> findAllWithEagerRelationships(Pageable page);

    @Query("SELECT * FROM transaction_account entity WHERE entity.parent_account_id = :id")
    Flux<TransactionAccount> findByParentAccount(Long id);

    @Query("SELECT * FROM transaction_account entity WHERE entity.parent_account_id IS NULL")
    Flux<TransactionAccount> findAllWhereParentAccountIsNull();

    @Query("SELECT * FROM transaction_account entity WHERE entity.transaction_account_type_id = :id")
    Flux<TransactionAccount> findByTransactionAccountType(Long id);

    @Query("SELECT * FROM transaction_account entity WHERE entity.transaction_account_type_id IS NULL")
    Flux<TransactionAccount> findAllWhereTransactionAccountTypeIsNull();

    @Query("SELECT * FROM transaction_account entity WHERE entity.transaction_currency_id = :id")
    Flux<TransactionAccount> findByTransactionCurrency(Long id);

    @Query("SELECT * FROM transaction_account entity WHERE entity.transaction_currency_id IS NULL")
    Flux<TransactionAccount> findAllWhereTransactionCurrencyIsNull();

    @Override
    <S extends TransactionAccount> Mono<S> save(S entity);

    @Override
    Flux<TransactionAccount> findAll();

    @Override
    Mono<TransactionAccount> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface TransactionAccountRepositoryInternal {
    <S extends TransactionAccount> Mono<S> save(S entity);

    Flux<TransactionAccount> findAllBy(Pageable pageable);

    Flux<TransactionAccount> findAll();

    Mono<TransactionAccount> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<TransactionAccount> findAllBy(Pageable pageable, Criteria criteria);

    Mono<TransactionAccount> findOneWithEagerRelationships(Long id);

    Flux<TransactionAccount> findAllWithEagerRelationships();

    Flux<TransactionAccount> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(Long id);
}
