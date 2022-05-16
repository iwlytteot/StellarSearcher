package cz.muni.fi.tovarys.model;

import java.util.HashMap;

/**
 * Representation that stores information about Table that belongs to Catalogue.
 */
public class Table extends Data {
    HashMap<String, String> columns = new HashMap<>();

    public HashMap<String, String> getColumns() {
        return columns;
    }

    public void setColumns(HashMap<String, String> columns) {
        this.columns = columns;
    }

    public Table() {
    }

    public Table(String name) {
        setName(name);
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

