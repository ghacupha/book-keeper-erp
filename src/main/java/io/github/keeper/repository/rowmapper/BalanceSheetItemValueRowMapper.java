package io.github.keeper.repository.rowmapper;

import io.github.keeper.domain.BalanceSheetItemValue;
import io.r2dbc.spi.Row;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link BalanceSheetItemValue}, with proper type conversions.
 */
@Service
public class BalanceSheetItemValueRowMapper implements BiFunction<Row, String, BalanceSheetItemValue> {

    private final ColumnConverter converter;

    public BalanceSheetItemValueRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link BalanceSheetItemValue} stored in the database.
     */
    @Override
    public BalanceSheetItemValue apply(Row row, String prefix) {
        BalanceSheetItemValue entity = new BalanceSheetItemValue();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setShortDescription(converter.fromRow(row, prefix + "_short_description", String.class));
        entity.setEffectiveDate(converter.fromRow(row, prefix + "_effective_date", LocalDate.class));
        entity.setItemAmount(converter.fromRow(row, prefix + "_item_amount", BigDecimal.class));
        entity.setItemTypeId(converter.fromRow(row, prefix + "_item_type_id", Long.class));
        return entity;
    }
}
