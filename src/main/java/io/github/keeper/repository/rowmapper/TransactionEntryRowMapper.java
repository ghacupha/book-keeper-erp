package io.github.keeper.repository.rowmapper;

import io.github.keeper.domain.TransactionEntry;
import io.github.keeper.domain.enumeration.TransactionEntryTypes;
import io.r2dbc.spi.Row;
import java.math.BigDecimal;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link TransactionEntry}, with proper type conversions.
 */
@Service
public class TransactionEntryRowMapper implements BiFunction<Row, String, TransactionEntry> {

    private final ColumnConverter converter;

    public TransactionEntryRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link TransactionEntry} stored in the database.
     */
    @Override
    public TransactionEntry apply(Row row, String prefix) {
        TransactionEntry entity = new TransactionEntry();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setEntryAmount(converter.fromRow(row, prefix + "_entry_amount", BigDecimal.class));
        entity.setTransactionEntryType(converter.fromRow(row, prefix + "_transaction_entry_type", TransactionEntryTypes.class));
        entity.setDescription(converter.fromRow(row, prefix + "_description", String.class));
        entity.setWasProposed(converter.fromRow(row, prefix + "_was_proposed", Boolean.class));
        entity.setWasPosted(converter.fromRow(row, prefix + "_was_posted", Boolean.class));
        entity.setWasDeleted(converter.fromRow(row, prefix + "_was_deleted", Boolean.class));
        entity.setWasApproved(converter.fromRow(row, prefix + "_was_approved", Boolean.class));
        entity.setTransactionAccountId(converter.fromRow(row, prefix + "_transaction_account_id", Long.class));
        entity.setAccountTransactionId(converter.fromRow(row, prefix + "_account_transaction_id", Long.class));
        return entity;
    }
}
