package io.github.keeper.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class TransactionAccountSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("account_name", table, columnPrefix + "_account_name"));
        columns.add(Column.aliased("account_number", table, columnPrefix + "_account_number"));
        columns.add(Column.aliased("opening_balance", table, columnPrefix + "_opening_balance"));

        columns.add(Column.aliased("parent_account_id", table, columnPrefix + "_parent_account_id"));
        columns.add(Column.aliased("transaction_account_type_id", table, columnPrefix + "_transaction_account_type_id"));
        columns.add(Column.aliased("transaction_currency_id", table, columnPrefix + "_transaction_currency_id"));
        return columns;
    }
}
