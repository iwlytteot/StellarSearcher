package model;

import cds.savot.model.SavotField;
import cds.savot.model.SavotResource;
import cds.savot.model.SavotTable;
import cds.savot.pull.SavotPullEngine;
import cds.savot.pull.SavotPullParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Catalogue extends Data{
    List<Table> tables = new ArrayList<>();

    public Catalogue() {
    }
    public Catalogue(Catalogue catalogue) {
        setName(catalogue.getName());
        setInfo(catalogue.getInfo());
        setTables(catalogue.getTables());
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public void addTable(Table table) {
        this.tables.add(table);
    }

    @Override
    public String toString() {
        return getName();
    }

    public static List<Catalogue> parseMetaData(String path) {
        var output = new ArrayList<Catalogue>();
        var catalogues = new SavotPullParser(path, SavotPullEngine.FULL).getVOTable().getResources();
        for (var item : catalogues.getItems()) {
            var newCatalogue = new Catalogue();
            var catalogue = (SavotResource) item;
            newCatalogue.setName(catalogue.getName());
            newCatalogue.setInfo(catalogue.getDescription());
            for (var tableItem : catalogue.getTables().getItems()) {
                var newTable = new Table();
                var table = (SavotTable) tableItem;
                newTable.setName(table.getName());
                newTable.setInfo(table.getDescription());
                var columns = new HashMap<String, String>();
                for (var columnItem : table.getFields().getItems()) {
                    var column = (SavotField) columnItem;
                    columns.put(column.getName(), column.getDescription());
                }
                newTable.setColumns(columns);
                newCatalogue.addTable(newTable);
            }
            output.add(newCatalogue);
        }
        return output;
    }
}

