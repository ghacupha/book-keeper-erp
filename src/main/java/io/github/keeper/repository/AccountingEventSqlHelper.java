package io.github.keeper.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class AccountingEventSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("event_date", table, columnPrefix + "_event_date"));

        columns.add(Column.aliased("event_type_id", table, columnPrefix + "_event_type_id"));
        columns.add(Column.aliased("dealer_id", table, columnPrefix + "_dealer_id"));
        return columns;
    }
}
