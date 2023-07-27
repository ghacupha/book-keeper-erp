package io.github.keeper.repository;

import io.github.keeper.domain.TransactionEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the TransactionEntry entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TransactionEntryRepository extends ReactiveCrudRepository<TransactionEntry, Long>, TransactionEntryRepositoryInternal {
    Flux<TransactionEntry> findAllBy(Pageable pageable);

    @Override
    Mono<TransactionEntry> findOneWithEagerRelationships(Long id);

    @Override
    Flux<TransactionEntry> findAllWithEagerRelationships();

    @Override
    Flux<TransactionEntry> findAllWithEagerRelationships(Pageable page);

    @Query("SELECT * FROM transaction_entry entity WHERE entity.transaction_account_id = :id")
    Flux<TransactionEntry> findByTransactionAccount(Long id);

    @Query("SELECT * FROM transaction_entry entity WHERE entity.transaction_account_id IS NULL")
    Flux<TransactionEntry> findAllWhereTransactionAccountIsNull();

    @Query("SELECT * FROM transaction_entry entity WHERE entity.account_transaction_id = :id")
    Flux<TransactionEntry> findByAccountTransaction(Long id);

    @Query("SELECT * FROM transaction_entry entity WHERE entity.account_transaction_id IS NULL")
    Flux<TransactionEntry> findAllWhereAccountTransactionIsNull();

    @Override
    <S extends TransactionEntry> Mono<S> save(S entity);

    @Override
    Flux<TransactionEntry> findAll();

    @Override
    Mono<TransactionEntry> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface TransactionEntryRepositoryInternal {
    <S extends TransactionEntry> Mono<S> save(S entity);

    Flux<TransactionEntry> findAllBy(Pageable pageable);

    Flux<TransactionEntry> findAll();

    Mono<TransactionEntry> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<TransactionEntry> findAllBy(Pageable pageable, Criteria criteria);

    Mono<TransactionEntry> findOneWithEagerRelationships(Long id);

    Flux<TransactionEntry> findAllWithEagerRelationships();

    Flux<TransactionEntry> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(Long id);
}
