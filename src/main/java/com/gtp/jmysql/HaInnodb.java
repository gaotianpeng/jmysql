package com.gtp.jmysql;

import com.gtp.jmysql.core.DTuple;
import com.gtp.jmysql.core.DTupleUtil;
import com.gtp.jmysql.core.PageUtil;
import com.gtp.jmysql.core.SpaceUtil;
import com.gtp.jmysql.dict.DictColumn;
import com.gtp.jmysql.dict.DictTable;
import com.gtp.jmysql.dict.SystemDict;

import com.gtp.jmysql.page.FspHdrPage;
import com.gtp.jmysql.page.IndexPage;
import com.gtp.jmysql.page.Page;
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

        // 生成新id
        int tableId = SystemDict.getInstance().addMaxTableId();
        int spaceId = SystemDict.getInstance().addMaxSpaceId();

        // 创建ibd文件，返回文件路径
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

        // 将当前创建的表元数据添加到数据字典
        dictTable.setDictColumnList(dictColumnList);
        SystemDict.getInstance().getNameTables().put(tableName, dictTable);
        SystemDict.getInstance().getIdTables().put(tableId, dictTable);
        SystemDict.getInstance().getSpaceIdTables().put(spaceId, dictTable);

        SystemDict.getInstance().serialize();

        // 初始化FspHdrPage
        FspHdrPage fspHdrPage = SpaceUtil.getFspHdrPage(spaceId);
        fspHdrPage.init_file_header(spaceId, 0);
        fspHdrPage.fil_page_set_type(8);
        fspHdrPage.set_fsp_size(1);
        PageUtil.flushPages(fspHdrPage);
    }

    public void insert(Insert insertStmt) {
        // 插入到哪个表空间的哪一页
        String tableName = insertStmt.getTable().getName();
        DictTable dictTable = SystemDict.getInstance().getNameTables().get(tableName);

        // 将insert转成DTuple，里面包含了插入的字段和数据
        DTuple dTuple = DTupleUtil.convert(insertStmt);

        // 先写死页号，后面实现B+树后，通过B+树来定位要插入的pageNo, 并且还要判断页是否满足了
        int pageNo = 1;
        if (SpaceUtil.getFspHdrPage(dictTable.getSpaceId()).get_fsp_size() == 1) {
            pageNo = PageUtil.createPage(dictTable.getSpaceId());
        }
        IndexPage indexPage = PageUtil.readPage(dictTable.getSpaceId(), pageNo);

        // 插入记录
        PageUtil.insert_row(indexPage, dTuple);
    }

    public void selectOne(PlainSelect plainSelectStmt) {
    }

    public void alter(Alter alterStmt) {

    }
}
