package io.github.keeper.repository.rowmapper;

import io.github.keeper.domain.AccountTransaction;
import io.r2dbc.spi.Row;
import java.time.LocalDate;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link AccountTransaction}, with proper type conversions.
 */
@Service
public class AccountTransactionRowMapper implements BiFunction<Row, String, AccountTransaction> {

    private final ColumnConverter converter;

    public AccountTransactionRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link AccountTransaction} stored in the database.
     */
    @Override
    public AccountTransaction apply(Row row, String prefix) {
        AccountTransaction entity = new AccountTransaction();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setTransactionDate(converter.fromRow(row, prefix + "_transaction_date", LocalDate.class));
        entity.setDescription(converter.fromRow(row, prefix + "_description", String.class));
        entity.setReferenceNumber(converter.fromRow(row, prefix + "_reference_number", String.class));
        entity.setWasProposed(converter.fromRow(row, prefix + "_was_proposed", Boolean.class));
        entity.setWasPosted(converter.fromRow(row, prefix + "_was_posted", Boolean.class));
        entity.setWasDeleted(converter.fromRow(row, prefix + "_was_deleted", Boolean.class));
        entity.setWasApproved(converter.fromRow(row, prefix + "_was_approved", Boolean.class));
        return entity;
    }
}
