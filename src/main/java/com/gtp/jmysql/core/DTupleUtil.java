package com.gtp.jmysql.core;

import com.gtp.jmysql.dict.DictColumn;
import com.gtp.jmysql.dict.DictTable;
import com.gtp.jmysql.dict.SystemDict;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.schema.Column;


import java.util.Optional;

public class DTupleUtil {
    public static DTuple convert(Insert insertStmt) {
        DTuple dTuple = new DTuple();

        String tableName = insertStmt.getTable().getName();
        ExpressionList<Column> columns = insertStmt.getColumns();
        ExpressionList<?> values = insertStmt.getValues().getExpressions();
        DictTable dictTable = SystemDict.getInstance().getNameTables().get(tableName);

        for (int i = 0; i < columns.size(); ++i) {
            Column column = columns.get(i);
            Expression value = values.get(i);


            Optional<DictColumn> dictColumn =
                    dictTable.getDictColumnList().stream().filter(
                            c -> c.getName().equals(column.getColumnName())).findFirst();
            if (dictColumn.isPresent()) {
                DField dField = new DField();
                dField.setDictColumn(dictColumn.get());

                if (!(value instanceof NullValue)) {
                    if (dictColumn.get().isInt()) {
                        dField.setData(Integer.valueOf(value.toString()));
                    } else {
                        dField.setData(((StringValue)value).getValue());
                    }
                } else {
                    // 如果插入的是NULL, 用两个00表示
                    dField.setData("00");
                }

                dTuple.getFields().add(dField);
            } else {
                throw new RuntimeException(String.format("%s字段不存在", column.getColumnName()));
            }
        }

        return dTuple;
    }
}
