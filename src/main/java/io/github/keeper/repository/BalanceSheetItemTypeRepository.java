package io.github.keeper.repository;

import io.github.keeper.domain.BalanceSheetItemType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the BalanceSheetItemType entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BalanceSheetItemTypeRepository
    extends ReactiveCrudRepository<BalanceSheetItemType, Long>, BalanceSheetItemTypeRepositoryInternal {
    Flux<BalanceSheetItemType> findAllBy(Pageable pageable);

    @Override
    Mono<BalanceSheetItemType> findOneWithEagerRelationships(Long id);

    @Override
    Flux<BalanceSheetItemType> findAllWithEagerRelationships();

    @Override
    Flux<BalanceSheetItemType> findAllWithEagerRelationships(Pageable page);

    @Query("SELECT * FROM balance_sheet_item_type entity WHERE entity.transaction_account_id = :id")
    Flux<BalanceSheetItemType> findByTransactionAccount(Long id);

    @Query("SELECT * FROM balance_sheet_item_type entity WHERE entity.transaction_account_id IS NULL")
    Flux<BalanceSheetItemType> findAllWhereTransactionAccountIsNull();

    @Query("SELECT * FROM balance_sheet_item_type entity WHERE entity.parent_item_id = :id")
    Flux<BalanceSheetItemType> findByParentItem(Long id);

    @Query("SELECT * FROM balance_sheet_item_type entity WHERE entity.parent_item_id IS NULL")
    Flux<BalanceSheetItemType> findAllWhereParentItemIsNull();

    @Override
    <S extends BalanceSheetItemType> Mono<S> save(S entity);

    @Override
    Flux<BalanceSheetItemType> findAll();

    @Override
    Mono<BalanceSheetItemType> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface BalanceSheetItemTypeRepositoryInternal {
    <S extends BalanceSheetItemType> Mono<S> save(S entity);

    Flux<BalanceSheetItemType> findAllBy(Pageable pageable);

    Flux<BalanceSheetItemType> findAll();

    Mono<BalanceSheetItemType> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<BalanceSheetItemType> findAllBy(Pageable pageable, Criteria criteria);

    Mono<BalanceSheetItemType> findOneWithEagerRelationships(Long id);

    Flux<BalanceSheetItemType> findAllWithEagerRelationships();

    Flux<BalanceSheetItemType> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(Long id);
}
