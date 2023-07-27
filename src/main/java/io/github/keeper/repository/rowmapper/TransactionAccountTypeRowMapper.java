package io.github.keeper.repository.rowmapper;

import io.github.keeper.domain.TransactionAccountType;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link TransactionAccountType}, with proper type conversions.
 */
@Service
public class TransactionAccountTypeRowMapper implements BiFunction<Row, String, TransactionAccountType> {

    private final ColumnConverter converter;

    public TransactionAccountTypeRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link TransactionAccountType} stored in the database.
     */
    @Override
    public TransactionAccountType apply(Row row, String prefix) {
        TransactionAccountType entity = new TransactionAccountType();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setName(converter.fromRow(row, prefix + "_name", String.class));
        return entity;
    }
}
