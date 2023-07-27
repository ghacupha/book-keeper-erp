package io.github.keeper.repository.rowmapper;

import io.github.keeper.domain.Dealer;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Dealer}, with proper type conversions.
 */
@Service
public class DealerRowMapper implements BiFunction<Row, String, Dealer> {

    private final ColumnConverter converter;

    public DealerRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Dealer} stored in the database.
     */
    @Override
    public Dealer apply(Row row, String prefix) {
        Dealer entity = new Dealer();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setName(converter.fromRow(row, prefix + "_name", String.class));
        entity.setDealerTypeId(converter.fromRow(row, prefix + "_dealer_type_id", Long.class));
        return entity;
    }
}
