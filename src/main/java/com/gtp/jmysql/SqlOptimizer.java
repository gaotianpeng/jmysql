package com.gtp.jmysql;

import net.sf.jsqlparser.statement.select.PlainSelect;

public class SqlOptimizer {
    public QEP_TAB optimize(PlainSelect plainSelect) {
        return new QEP_TAB(plainSelect);
    }

    static class QEP_TAB {
        private final SqlExecutor sqlExecutor = new SqlExecutor();

        private PlainSelect plainSelectStmt;

        public QEP_TAB(PlainSelect plainSelectStmt) {
            this.plainSelectStmt = plainSelectStmt;
        }

        public void exec() {
            sqlExecutor.select(plainSelectStmt);
        }
    }
}
