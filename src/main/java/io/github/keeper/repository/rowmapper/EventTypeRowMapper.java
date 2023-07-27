package io.github.keeper.repository.rowmapper;

import io.github.keeper.domain.EventType;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link EventType}, with proper type conversions.
 */
@Service
public class EventTypeRowMapper implements BiFunction<Row, String, EventType> {

    private final ColumnConverter converter;

    public EventTypeRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link EventType} stored in the database.
     */
    @Override
    public EventType apply(Row row, String prefix) {
        EventType entity = new EventType();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setName(converter.fromRow(row, prefix + "_name", String.class));
        return entity;
    }
}
