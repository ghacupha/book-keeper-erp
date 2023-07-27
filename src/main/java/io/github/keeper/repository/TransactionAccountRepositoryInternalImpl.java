package io.github.keeper.repository;

import static org.springframework.data.relational.core.query.Criteria.where;

import io.github.keeper.domain.TransactionAccount;
import io.github.keeper.repository.rowmapper.TransactionAccountRowMapper;
import io.github.keeper.repository.rowmapper.TransactionAccountRowMapper;
import io.github.keeper.repository.rowmapper.TransactionAccountTypeRowMapper;
import io.github.keeper.repository.rowmapper.TransactionCurrencyRowMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.math.BigDecimal;
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
 * Spring Data R2DBC custom repository implementation for the TransactionAccount entity.
 */
@SuppressWarnings("unused")
class TransactionAccountRepositoryInternalImpl
    extends SimpleR2dbcRepository<TransactionAccount, Long>
    implements TransactionAccountRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final TransactionAccountRowMapper transactionaccountMapper;
    private final TransactionAccountTypeRowMapper transactionaccounttypeMapper;
    private final TransactionCurrencyRowMapper transactioncurrencyMapper;

    private static final Table entityTable = Table.aliased("transaction_account", EntityManager.ENTITY_ALIAS);
    private static final Table parentAccountTable = Table.aliased("transaction_account", "parentAccount");
    private static final Table transactionAccountTypeTable = Table.aliased("transaction_account_type", "transactionAccountType");
    private static final Table transactionCurrencyTable = Table.aliased("transaction_currency", "transactionCurrency");

    public TransactionAccountRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        TransactionAccountRowMapper transactionaccountMapper,
        TransactionAccountTypeRowMapper transactionaccounttypeMapper,
        TransactionCurrencyRowMapper transactioncurrencyMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(TransactionAccount.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.transactionaccountMapper = transactionaccountMapper;
        this.transactionaccounttypeMapper = transactionaccounttypeMapper;
        this.transactioncurrencyMapper = transactioncurrencyMapper;
    }

    @Override
    public Flux<TransactionAccount> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<TransactionAccount> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = TransactionAccountSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(TransactionAccountSqlHelper.getColumns(parentAccountTable, "parentAccount"));
        columns.addAll(TransactionAccountTypeSqlHelper.getColumns(transactionAccountTypeTable, "transactionAccountType"));
        columns.addAll(TransactionCurrencySqlHelper.getColumns(transactionCurrencyTable, "transactionCurrency"));
        SelectFromAndJoinCondition selectFrom = Select
            .builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(parentAccountTable)
            .on(Column.create("parent_account_id", entityTable))
            .equals(Column.create("id", parentAccountTable))
            .leftOuterJoin(transactionAccountTypeTable)
            .on(Column.create("transaction_account_type_id", entityTable))
            .equals(Column.create("id", transactionAccountTypeTable))
            .leftOuterJoin(transactionCurrencyTable)
            .on(Column.create("transaction_currency_id", entityTable))
            .equals(Column.create("id", transactionCurrencyTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, TransactionAccount.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<TransactionAccount> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<TransactionAccount> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    @Override
    public Mono<TransactionAccount> findOneWithEagerRelationships(Long id) {
        return findById(id);
    }

    @Override
    public Flux<TransactionAccount> findAllWithEagerRelationships() {
        return findAll();
    }

    @Override
    public Flux<TransactionAccount> findAllWithEagerRelationships(Pageable page) {
        return findAllBy(page);
    }

    private TransactionAccount process(Row row, RowMetadata metadata) {
        TransactionAccount entity = transactionaccountMapper.apply(row, "e");
        entity.setParentAccount(transactionaccountMapper.apply(row, "parentAccount"));
        entity.setTransactionAccountType(transactionaccounttypeMapper.apply(row, "transactionAccountType"));
        entity.setTransactionCurrency(transactioncurrencyMapper.apply(row, "transactionCurrency"));
        return entity;
    }

    @Override
    public <S extends TransactionAccount> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
