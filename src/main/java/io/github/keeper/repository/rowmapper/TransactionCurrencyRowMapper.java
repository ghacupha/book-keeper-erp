package io.github.keeper.repository.rowmapper;

import io.github.keeper.domain.TransactionCurrency;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link TransactionCurrency}, with proper type conversions.
 */
@Service
public class TransactionCurrencyRowMapper implements BiFunction<Row, String, TransactionCurrency> {

    private final ColumnConverter converter;

    public TransactionCurrencyRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link TransactionCurrency} stored in the database.
     */
    @Override
    public TransactionCurrency apply(Row row, String prefix) {
        TransactionCurrency entity = new TransactionCurrency();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setName(converter.fromRow(row, prefix + "_name", String.class));
        entity.setCode(converter.fromRow(row, prefix + "_code", String.class));
        return entity;
    }
}
