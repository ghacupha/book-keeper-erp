package io.github.keeper.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class BalanceSheetItemTypeSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("item_sequence", table, columnPrefix + "_item_sequence"));
        columns.add(Column.aliased("item_number", table, columnPrefix + "_item_number"));
        columns.add(Column.aliased("short_description", table, columnPrefix + "_short_description"));

        columns.add(Column.aliased("transaction_account_id", table, columnPrefix + "_transaction_account_id"));
        columns.add(Column.aliased("parent_item_id", table, columnPrefix + "_parent_item_id"));
        return columns;
    }
}
