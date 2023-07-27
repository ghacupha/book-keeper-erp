package io.github.keeper.repository.rowmapper;

import io.github.keeper.domain.DealerType;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link DealerType}, with proper type conversions.
 */
@Service
public class DealerTypeRowMapper implements BiFunction<Row, String, DealerType> {

    private final ColumnConverter converter;

    public DealerTypeRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link DealerType} stored in the database.
     */
    @Override
    public DealerType apply(Row row, String prefix) {
        DealerType entity = new DealerType();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setName(converter.fromRow(row, prefix + "_name", String.class));
        return entity;
    }
}
