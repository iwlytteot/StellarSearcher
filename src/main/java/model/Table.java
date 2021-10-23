package model;

import java.util.HashMap;

public class Table extends Data {
    HashMap<String, String> columns = new HashMap<>();

    public HashMap<String, String> getColumns() {
        return columns;
    }

    public void setColumns(HashMap<String, String> columns) {
        this.columns = columns;
    }

    public void addColumn(String columnName) {
        columns.put(columnName, "");
    }

    public Table() {
    }

    public Table(Table table) {
        setName(table.getName());
        setInfo(table.getInfo());
        setColumns(table.getColumns());
    }

    @Override
    public String toString() {
        return getName();
    }
}

