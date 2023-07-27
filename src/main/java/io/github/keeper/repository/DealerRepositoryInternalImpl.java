package io.github.keeper.repository;

import static org.springframework.data.relational.core.query.Criteria.where;

import io.github.keeper.domain.Dealer;
import io.github.keeper.repository.rowmapper.DealerRowMapper;
import io.github.keeper.repository.rowmapper.DealerTypeRowMapper;
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
 * Spring Data R2DBC custom repository implementation for the Dealer entity.
 */
@SuppressWarnings("unused")
class DealerRepositoryInternalImpl extends SimpleR2dbcRepository<Dealer, Long> implements DealerRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final DealerTypeRowMapper dealertypeMapper;
    private final DealerRowMapper dealerMapper;

    private static final Table entityTable = Table.aliased("dealer", EntityManager.ENTITY_ALIAS);
    private static final Table dealerTypeTable = Table.aliased("dealer_type", "dealerType");

    public DealerRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        DealerTypeRowMapper dealertypeMapper,
        DealerRowMapper dealerMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Dealer.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.dealertypeMapper = dealertypeMapper;
        this.dealerMapper = dealerMapper;
    }

    @Override
    public Flux<Dealer> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Dealer> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = DealerSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(DealerTypeSqlHelper.getColumns(dealerTypeTable, "dealerType"));
        SelectFromAndJoinCondition selectFrom = Select
            .builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(dealerTypeTable)
            .on(Column.create("dealer_type_id", entityTable))
            .equals(Column.create("id", dealerTypeTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Dealer.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Dealer> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Dealer> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    @Override
    public Mono<Dealer> findOneWithEagerRelationships(Long id) {
        return findById(id);
    }

    @Override
    public Flux<Dealer> findAllWithEagerRelationships() {
        return findAll();
    }

    @Override
    public Flux<Dealer> findAllWithEagerRelationships(Pageable page) {
        return findAllBy(page);
    }

    private Dealer process(Row row, RowMetadata metadata) {
        Dealer entity = dealerMapper.apply(row, "e");
        entity.setDealerType(dealertypeMapper.apply(row, "dealerType"));
        return entity;
    }

    @Override
    public <S extends Dealer> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
