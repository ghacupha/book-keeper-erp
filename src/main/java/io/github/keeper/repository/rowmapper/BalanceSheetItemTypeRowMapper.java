package io.github.keeper.repository.rowmapper;

import io.github.keeper.domain.BalanceSheetItemType;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link BalanceSheetItemType}, with proper type conversions.
 */
@Service
public class BalanceSheetItemTypeRowMapper implements BiFunction<Row, String, BalanceSheetItemType> {

    private final ColumnConverter converter;

    public BalanceSheetItemTypeRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link BalanceSheetItemType} stored in the database.
     */
    @Override
    public BalanceSheetItemType apply(Row row, String prefix) {
        BalanceSheetItemType entity = new BalanceSheetItemType();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setItemSequence(converter.fromRow(row, prefix + "_item_sequence", Integer.class));
        entity.setItemNumber(converter.fromRow(row, prefix + "_item_number", String.class));
        entity.setShortDescription(converter.fromRow(row, prefix + "_short_description", String.class));
        entity.setTransactionAccountId(converter.fromRow(row, prefix + "_transaction_account_id", Long.class));
        entity.setParentItemId(converter.fromRow(row, prefix + "_parent_item_id", Long.class));
        return entity;
    }
}
