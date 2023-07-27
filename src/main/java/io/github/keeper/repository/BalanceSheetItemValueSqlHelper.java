package io.github.keeper.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class BalanceSheetItemValueSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("short_description", table, columnPrefix + "_short_description"));
        columns.add(Column.aliased("effective_date", table, columnPrefix + "_effective_date"));
        columns.add(Column.aliased("item_amount", table, columnPrefix + "_item_amount"));

        columns.add(Column.aliased("item_type_id", table, columnPrefix + "_item_type_id"));
        return columns;
    }
}
