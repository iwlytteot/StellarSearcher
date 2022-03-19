package model;

import cds.savot.model.SavotField;
import cds.savot.model.SavotResource;
import cds.savot.model.SavotTable;
import cds.savot.pull.SavotPullEngine;
import cds.savot.pull.SavotPullParser;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Representation for Catalogue. This is unified for VizieR, MAST and SIMBAD catalogues/database respectively.
 */
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

    /**
     * Method for parsing meta data (relative only for VizieR). Method parses data into Catalogue.
     * These data can be multiple catalogues or its tables. Also columns are retrieved and saved into new instance
     * of Table, that is then put into Catalogue.
     * @param input VOTable object in String representation
     * @return List of VizieR catalogues and its tables
     */
    public static List<Catalogue> parseMetaData(String input) {
        var output = new ArrayList<Catalogue>();
        var catalogues = new SavotPullParser(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                SavotPullEngine.FULL, "UTF-8").getVOTable().getResources();
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

