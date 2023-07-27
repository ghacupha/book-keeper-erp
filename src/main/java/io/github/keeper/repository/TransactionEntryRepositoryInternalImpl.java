package io.github.keeper.repository;

import static org.springframework.data.relational.core.query.Criteria.where;

import io.github.keeper.domain.TransactionEntry;
import io.github.keeper.domain.enumeration.TransactionEntryTypes;
import io.github.keeper.repository.rowmapper.AccountTransactionRowMapper;
import io.github.keeper.repository.rowmapper.TransactionAccountRowMapper;
import io.github.keeper.repository.rowmapper.TransactionEntryRowMapper;
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
 * Spring Data R2DBC custom repository implementation for the TransactionEntry entity.
 */
@SuppressWarnings("unused")
class TransactionEntryRepositoryInternalImpl
    extends SimpleR2dbcRepository<TransactionEntry, Long>
    implements TransactionEntryRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final TransactionAccountRowMapper transactionaccountMapper;
    private final AccountTransactionRowMapper accounttransactionMapper;
    private final TransactionEntryRowMapper transactionentryMapper;

    private static final Table entityTable = Table.aliased("transaction_entry", EntityManager.ENTITY_ALIAS);
    private static final Table transactionAccountTable = Table.aliased("transaction_account", "transactionAccount");
    private static final Table accountTransactionTable = Table.aliased("account_transaction", "accountTransaction");

    public TransactionEntryRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        TransactionAccountRowMapper transactionaccountMapper,
        AccountTransactionRowMapper accounttransactionMapper,
        TransactionEntryRowMapper transactionentryMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(TransactionEntry.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.transactionaccountMapper = transactionaccountMapper;
        this.accounttransactionMapper = accounttransactionMapper;
        this.transactionentryMapper = transactionentryMapper;
    }

    @Override
    public Flux<TransactionEntry> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<TransactionEntry> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = TransactionEntrySqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(TransactionAccountSqlHelper.getColumns(transactionAccountTable, "transactionAccount"));
        columns.addAll(AccountTransactionSqlHelper.getColumns(accountTransactionTable, "accountTransaction"));
        SelectFromAndJoinCondition selectFrom = Select
            .builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(transactionAccountTable)
            .on(Column.create("transaction_account_id", entityTable))
            .equals(Column.create("id", transactionAccountTable))
            .leftOuterJoin(accountTransactionTable)
            .on(Column.create("account_transaction_id", entityTable))
            .equals(Column.create("id", accountTransactionTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, TransactionEntry.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<TransactionEntry> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<TransactionEntry> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    @Override
    public Mono<TransactionEntry> findOneWithEagerRelationships(Long id) {
        return findById(id);
    }

    @Override
    public Flux<TransactionEntry> findAllWithEagerRelationships() {
        return findAll();
    }

    @Override
    public Flux<TransactionEntry> findAllWithEagerRelationships(Pageable page) {
        return findAllBy(page);
    }

    private TransactionEntry process(Row row, RowMetadata metadata) {
        TransactionEntry entity = transactionentryMapper.apply(row, "e");
        entity.setTransactionAccount(transactionaccountMapper.apply(row, "transactionAccount"));
        entity.setAccountTransaction(accounttransactionMapper.apply(row, "accountTransaction"));
        return entity;
    }

    @Override
    public <S extends TransactionEntry> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
