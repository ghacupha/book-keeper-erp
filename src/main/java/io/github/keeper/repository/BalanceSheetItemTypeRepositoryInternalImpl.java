package io.github.keeper.repository;

import static org.springframework.data.relational.core.query.Criteria.where;

import io.github.keeper.domain.BalanceSheetItemType;
import io.github.keeper.repository.rowmapper.BalanceSheetItemTypeRowMapper;
import io.github.keeper.repository.rowmapper.BalanceSheetItemTypeRowMapper;
import io.github.keeper.repository.rowmapper.TransactionAccountRowMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Comparison;
import org.springframework.data.relational.core.sql.Condition;
import org.springframework.data.relational.core.sql.Conditions;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC custom repository implementation for the BalanceSheetItemType entity.
 */
@SuppressWarnings("unused")
class BalanceSheetItemTypeRepositoryInternalImpl
    extends SimpleR2dbcRepository<BalanceSheetItemType, Long>
    implements BalanceSheetItemTypeRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final TransactionAccountRowMapper transactionaccountMapper;
    private final BalanceSheetItemTypeRowMapper balancesheetitemtypeMapper;

    private static final Table entityTable = Table.aliased("balance_sheet_item_type", EntityManager.ENTITY_ALIAS);
    private static final Table transactionAccountTable = Table.aliased("transaction_account", "transactionAccount");
    private static final Table parentItemTable = Table.aliased("balance_sheet_item_type", "parentItem");

    public BalanceSheetItemTypeRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        TransactionAccountRowMapper transactionaccountMapper,
        BalanceSheetItemTypeRowMapper balancesheetitemtypeMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(BalanceSheetItemType.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.transactionaccountMapper = transactionaccountMapper;
        this.balancesheetitemtypeMapper = balancesheetitemtypeMapper;
    }

    @Override
    public Flux<BalanceSheetItemType> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<BalanceSheetItemType> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = BalanceSheetItemTypeSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(TransactionAccountSqlHelper.getColumns(transactionAccountTable, "transactionAccount"));
        columns.addAll(BalanceSheetItemTypeSqlHelper.getColumns(parentItemTable, "parentItem"));
        SelectFromAndJoinCondition selectFrom = Select
            .builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(transactionAccountTable)
            .on(Column.create("transaction_account_id", entityTable))
            .equals(Column.create("id", transactionAccountTable))
            .leftOuterJoin(parentItemTable)
            .on(Column.create("parent_item_id", entityTable))
            .equals(Column.create("id", parentItemTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, BalanceSheetItemType.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<BalanceSheetItemType> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<BalanceSheetItemType> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    @Override
    public Mono<BalanceSheetItemType> findOneWithEagerRelationships(Long id) {
        return findById(id);
    }

    @Override
    public Flux<BalanceSheetItemType> findAllWithEagerRelationships() {
        return findAll();
    }

    @Override
    public Flux<BalanceSheetItemType> findAllWithEagerRelationships(Pageable page) {
        return findAllBy(page);
    }

    private BalanceSheetItemType process(Row row, RowMetadata metadata) {
        BalanceSheetItemType entity = balancesheetitemtypeMapper.apply(row, "e");
        entity.setTransactionAccount(transactionaccountMapper.apply(row, "transactionAccount"));
        entity.setParentItem(balancesheetitemtypeMapper.apply(row, "parentItem"));
        return entity;
    }

    @Override
    public <S extends BalanceSheetItemType> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
