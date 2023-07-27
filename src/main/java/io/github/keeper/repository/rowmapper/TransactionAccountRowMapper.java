package io.github.keeper.repository.rowmapper;

import io.github.keeper.domain.TransactionAccount;
import io.r2dbc.spi.Row;
import java.math.BigDecimal;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link TransactionAccount}, with proper type conversions.
 */
@Service
public class TransactionAccountRowMapper implements BiFunction<Row, String, TransactionAccount> {

    private final ColumnConverter converter;

    public TransactionAccountRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link TransactionAccount} stored in the database.
     */
    @Override
    public TransactionAccount apply(Row row, String prefix) {
        TransactionAccount entity = new TransactionAccount();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setAccountName(converter.fromRow(row, prefix + "_account_name", String.class));
        entity.setAccountNumber(converter.fromRow(row, prefix + "_account_number", String.class));
        entity.setOpeningBalance(converter.fromRow(row, prefix + "_opening_balance", BigDecimal.class));
        entity.setParentAccountId(converter.fromRow(row, prefix + "_parent_account_id", Long.class));
        entity.setTransactionAccountTypeId(converter.fromRow(row, prefix + "_transaction_account_type_id", Long.class));
        entity.setTransactionCurrencyId(converter.fromRow(row, prefix + "_transaction_currency_id", Long.class));
        return entity;
    }
}
