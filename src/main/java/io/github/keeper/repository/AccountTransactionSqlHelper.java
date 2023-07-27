package io.github.keeper.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class AccountTransactionSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("transaction_date", table, columnPrefix + "_transaction_date"));
        columns.add(Column.aliased("description", table, columnPrefix + "_description"));
        columns.add(Column.aliased("reference_number", table, columnPrefix + "_reference_number"));
        columns.add(Column.aliased("was_proposed", table, columnPrefix + "_was_proposed"));
        columns.add(Column.aliased("was_posted", table, columnPrefix + "_was_posted"));
        columns.add(Column.aliased("was_deleted", table, columnPrefix + "_was_deleted"));
        columns.add(Column.aliased("was_approved", table, columnPrefix + "_was_approved"));

        return columns;
    }
}
