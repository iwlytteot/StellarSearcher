package model;

import java.util.HashMap;

public class Table {
    String tableName;
    String tableInfo;
    HashMap<String, String> columns = new HashMap<>();

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableInfo() {
        return tableInfo;
    }

    public void setTableInfo(String tableInfo) {
        this.tableInfo = tableInfo;
    }

    public HashMap<String, String> getColumns() {
        return columns;
    }

    public void setColumns(HashMap<String, String> columns) {
        this.columns = columns;
    }

    public void addColumn(String columnName) {
        columns.put(columnName, "");
    }
}

