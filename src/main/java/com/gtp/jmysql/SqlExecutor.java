package com.gtp.jmysql;

import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class SqlExecutor {
    private HaInnodb haInnodb = new HaInnodb();

    public void createTable(CreateTable createTableStmt) {
        haInnodb.createTable(createTableStmt);
    }

    public void insert(Insert insertStmt) {
        haInnodb.insert(insertStmt);
    }

    public void select(PlainSelect plainSelectStmt) {
        haInnodb.selectOne(plainSelectStmt);
    }

    public void alter(Alter alterStmt) {
        haInnodb.alter(alterStmt);
    }
}
