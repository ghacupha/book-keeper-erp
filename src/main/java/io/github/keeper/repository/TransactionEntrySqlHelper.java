package io.github.keeper.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class TransactionEntrySqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("entry_amount", table, columnPrefix + "_entry_amount"));
        columns.add(Column.aliased("transaction_entry_type", table, columnPrefix + "_transaction_entry_type"));
        columns.add(Column.aliased("description", table, columnPrefix + "_description"));
        columns.add(Column.aliased("was_proposed", table, columnPrefix + "_was_proposed"));
        columns.add(Column.aliased("was_posted", table, columnPrefix + "_was_posted"));
        columns.add(Column.aliased("was_deleted", table, columnPrefix + "_was_deleted"));
        columns.add(Column.aliased("was_approved", table, columnPrefix + "_was_approved"));

        columns.add(Column.aliased("transaction_account_id", table, columnPrefix + "_transaction_account_id"));
        columns.add(Column.aliased("account_transaction_id", table, columnPrefix + "_account_transaction_id"));
        return columns;
    }
}
