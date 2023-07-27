package io.github.keeper.repository;

import static org.springframework.data.relational.core.query.Criteria.where;

import io.github.keeper.domain.BalanceSheetItemValue;
import io.github.keeper.repository.rowmapper.BalanceSheetItemTypeRowMapper;
import io.github.keeper.repository.rowmapper.BalanceSheetItemValueRowMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.math.BigDecimal;
import java.time.LocalDate;
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
 * Spring Data R2DBC custom repository implementation for the BalanceSheetItemValue entity.
 */
@SuppressWarnings("unused")
class BalanceSheetItemValueRepositoryInternalImpl
    extends SimpleR2dbcRepository<BalanceSheetItemValue, Long>
    implements BalanceSheetItemValueRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final BalanceSheetItemTypeRowMapper balancesheetitemtypeMapper;
    private final BalanceSheetItemValueRowMapper balancesheetitemvalueMapper;

    private static final Table entityTable = Table.aliased("balance_sheet_item_value", EntityManager.ENTITY_ALIAS);
    private static final Table itemTypeTable = Table.aliased("balance_sheet_item_type", "itemType");

    public BalanceSheetItemValueRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        BalanceSheetItemTypeRowMapper balancesheetitemtypeMapper,
        BalanceSheetItemValueRowMapper balancesheetitemvalueMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(BalanceSheetItemValue.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.balancesheetitemtypeMapper = balancesheetitemtypeMapper;
        this.balancesheetitemvalueMapper = balancesheetitemvalueMapper;
    }

    @Override
    public Flux<BalanceSheetItemValue> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<BalanceSheetItemValue> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = BalanceSheetItemValueSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(BalanceSheetItemTypeSqlHelper.getColumns(itemTypeTable, "itemType"));
        SelectFromAndJoinCondition selectFrom = Select
            .builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(itemTypeTable)
            .on(Column.create("item_type_id", entityTable))
            .equals(Column.create("id", itemTypeTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, BalanceSheetItemValue.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<BalanceSheetItemValue> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<BalanceSheetItemValue> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    @Override
    public Mono<BalanceSheetItemValue> findOneWithEagerRelationships(Long id) {
        return findById(id);
    }

    @Override
    public Flux<BalanceSheetItemValue> findAllWithEagerRelationships() {
        return findAll();
    }

    @Override
    public Flux<BalanceSheetItemValue> findAllWithEagerRelationships(Pageable page) {
        return findAllBy(page);
    }

    private BalanceSheetItemValue process(Row row, RowMetadata metadata) {
        BalanceSheetItemValue entity = balancesheetitemvalueMapper.apply(row, "e");
        entity.setItemType(balancesheetitemtypeMapper.apply(row, "itemType"));
        return entity;
    }

    @Override
    public <S extends BalanceSheetItemValue> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
