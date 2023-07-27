package io.github.keeper.repository;

import io.github.keeper.domain.BalanceSheetItemValue;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the BalanceSheetItemValue entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BalanceSheetItemValueRepository
    extends ReactiveCrudRepository<BalanceSheetItemValue, Long>, BalanceSheetItemValueRepositoryInternal {
    Flux<BalanceSheetItemValue> findAllBy(Pageable pageable);

    @Override
    Mono<BalanceSheetItemValue> findOneWithEagerRelationships(Long id);

    @Override
    Flux<BalanceSheetItemValue> findAllWithEagerRelationships();

    @Override
    Flux<BalanceSheetItemValue> findAllWithEagerRelationships(Pageable page);

    @Query("SELECT * FROM balance_sheet_item_value entity WHERE entity.item_type_id = :id")
    Flux<BalanceSheetItemValue> findByItemType(Long id);

    @Query("SELECT * FROM balance_sheet_item_value entity WHERE entity.item_type_id IS NULL")
    Flux<BalanceSheetItemValue> findAllWhereItemTypeIsNull();

    @Override
    <S extends BalanceSheetItemValue> Mono<S> save(S entity);

    @Override
    Flux<BalanceSheetItemValue> findAll();

    @Override
    Mono<BalanceSheetItemValue> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface BalanceSheetItemValueRepositoryInternal {
    <S extends BalanceSheetItemValue> Mono<S> save(S entity);

    Flux<BalanceSheetItemValue> findAllBy(Pageable pageable);

    Flux<BalanceSheetItemValue> findAll();

    Mono<BalanceSheetItemValue> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<BalanceSheetItemValue> findAllBy(Pageable pageable, Criteria criteria);

    Mono<BalanceSheetItemValue> findOneWithEagerRelationships(Long id);

    Flux<BalanceSheetItemValue> findAllWithEagerRelationships();

    Flux<BalanceSheetItemValue> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(Long id);
}
