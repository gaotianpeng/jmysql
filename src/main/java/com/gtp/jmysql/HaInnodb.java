package com.gtp.jmysql;

import cn.hutool.core.lang.Dict;
import com.gtp.jmysql.core.SpaceUtil;
import com.gtp.jmysql.dict.DictColumn;
import com.gtp.jmysql.dict.DictTable;
import com.gtp.jmysql.dict.SystemDict;

import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class HaInnodb {
    public void createTable(CreateTable createTableStmt) {
        String tableName = createTableStmt.getTable().getName();

        if (SystemDict.getInstance().getNameTables().containsKey(tableName)) {
            throw new RuntimeException("表已经存在");
        }

        int tableId = SystemDict.getInstance().addMaxTableId();
        int spaceId = SystemDict.getInstance().getMaxSpaceId();

        Path tableSpacePath = SpaceUtil.createUserTableSpace(tableName);

        DictTable dictTable = new DictTable();
        dictTable.setTableId(tableId);
        dictTable.setSpaceId(spaceId);
        dictTable.setTableName(tableName);
        dictTable.setPath(tableSpacePath.toString());

        List<DictColumn> dictColumnList = new ArrayList<>();
        List<ColumnDefinition> columnDefinitions = createTableStmt.getColumnDefinitions();
        for (int i = 0; i < columnDefinitions.size(); ++i) {
            ColumnDefinition columnDefinition = columnDefinitions.get(i);

            DictColumn dictColumn = new DictColumn();
            dictColumn.setTableId(tableId);
            dictColumn.setName(columnDefinition.getColumnName());
            String dataType = columnDefinition.getColDataType().getDataType();
            dictColumn.setType(dataType);
            dictColumn.setColNo(i);
            if ("int".equals(dataType)) {
                dictColumn.setLen(4);
            } else {
                // 实际应该解析出varchar(11)中的11，然后还要知道编码集，才能算出字段长度
                // 这里就直接写死了，方便模拟
                dictColumn.setLen(2);
            }
            dictColumnList.add(dictColumn);
        }

        dictTable.setDictColumnList(dictColumnList);
        SystemDict.getInstance().getNameTables().put(tableName, dictTable);
        SystemDict.getInstance().getIdTables().put(tableId, dictTable);
        SystemDict.getInstance().getSpaceIdTables().put(spaceId, dictTable);

        SystemDict.getInstance().serialize();
    }

    public void insert(Insert insertStmt) {

    }

    public void selectOne(PlainSelect plainSelectStmt) {
    }

    public void alter(Alter alterStmt) {
    }
}
