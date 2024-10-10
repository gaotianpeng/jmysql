package com.gtp.jmysql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;


public class SqlParser {

    private SqlOptimizer sqlOptimizer = new SqlOptimizer();
    private SqlExecutor sqlExecutor = new SqlExecutor();

    public String mysql_parse(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            return mysql_execute_command(statement);
        } catch (JSQLParserException e) {
            return "failed";
        }
    }

    public String mysql_execute_command(Statement statement) {

        if (statement instanceof CreateTable createTableStatement) {
            sqlExecutor.createTable(createTableStatement);
        } else if (statement instanceof Insert insertStatement) {
            sqlExecutor.insert(insertStatement);
        } else if (statement instanceof PlainSelect plainSelect) {
            SqlOptimizer.QEP_TAB qepTab = sqlOptimizer.optimize(plainSelect);
            qepTab.exec();
        } else if (statement instanceof Alter alterStatement) {
            sqlExecutor.alter(alterStatement);
        }

        return "success";
    }
}
