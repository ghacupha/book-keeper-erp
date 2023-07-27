package io.github.keeper.repository.rowmapper;

import io.github.keeper.domain.AccountingEvent;
import io.r2dbc.spi.Row;
import java.time.LocalDate;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link AccountingEvent}, with proper type conversions.
 */
@Service
public class AccountingEventRowMapper implements BiFunction<Row, String, AccountingEvent> {

    private final ColumnConverter converter;

    public AccountingEventRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link AccountingEvent} stored in the database.
     */
    @Override
    public AccountingEvent apply(Row row, String prefix) {
        AccountingEvent entity = new AccountingEvent();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setEventDate(converter.fromRow(row, prefix + "_event_date", LocalDate.class));
        entity.setEventTypeId(converter.fromRow(row, prefix + "_event_type_id", Long.class));
        entity.setDealerId(converter.fromRow(row, prefix + "_dealer_id", Long.class));
        return entity;
    }
}
