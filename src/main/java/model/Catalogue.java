package model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

    //maybe not the right place, just for saving code
    /**
     * Method to parse catalogue info from tsv-like file obtained from Vizier query
     *
     * Be careful, this is working by 1.1.2021. There are some bugs and inconsistency at Vizier side,
     * which I am unable to fix at this moment.
     * However, the fix should be easy and straightforward on either side (Vizier side and this parser).
     *
     * @param path Path to file which is being parsed
     */
    public static List<Catalogue> parseMetaData(String path) {
        try (InputStream inputStream = new FileInputStream(path);
            Scanner sc = new Scanner(inputStream, StandardCharsets.UTF_8)) {

            List<Catalogue> catalogues = new ArrayList<>();
            Catalogue currentCatalogue = new Catalogue();
            Table table = new Table();
            String line;
            String tableInfoBugged = "";
            boolean isCatalogue = false, firstRun = true;
            while (sc.hasNextLine()) {
                line = sc.nextLine();

                // new Catalogue
                if (line.contains("#RESOURCE=") && firstRun) {
                    firstRun = false;
                    isCatalogue = true;
                }
                else if (line.contains("#RESOURCE=")) {
                    currentCatalogue.addTable(table); //finish business with old catalogue and its tables
                    catalogues.add(currentCatalogue);
                    currentCatalogue = new Catalogue();
                    isCatalogue = true;
                }

                // new Table
                if (line.contains("#Table")) {
                    if (!isCatalogue) {
                        currentCatalogue.addTable(table);
                    }
                    table = new Table();

                    // dealing with bug when catalogue info contains also table info..
                    if (!tableInfoBugged.isEmpty()) {
                        table.setInfo(tableInfoBugged);
                        tableInfoBugged = "";
                    }

                    isCatalogue = false;
                }

                if (line.contains("#Name:")) {
                    if (isCatalogue) {
                        currentCatalogue.setName(line.substring(7));
                    } else {
                        table.setName(line.substring(7));
                    }
                }

                if (line.equals("#Title:")) {
                    line = sc.nextLine();
                    if (!line.contains("#INFO")) {
                        continue;  // we deal with broken search for one catalogue with no info about table
                    }
                    line = sc.nextLine();

                    if (!isCatalogue) {
                        table.setInfo(line.substring(1).stripLeading());
                    } else {
                        currentCatalogue.setInfo(line.substring(1).stripLeading());
                        line = sc.nextLine();
                        tableInfoBugged = line.substring(1).stripLeading();
                    }
                }
                else if (line.contains("#Title:")) {
                    currentCatalogue.setInfo(line.substring(8));
                }

                if (line.contains("#Column")) {
                    table.addColumn(line.substring(8, line.indexOf('\t', 8)));
                }

                if (line.contains("#INFO") && line.contains("Error=") && !line.contains("Error= STOP")) {
                    throw new CatalogueQueryException(line);
                }

            }

            if (sc.ioException() != null) {
                throw new CatalogueQueryException(sc.ioException().getMessage());
            }

            currentCatalogue.addTable(table);
            catalogues.add(currentCatalogue);

            return catalogues;

        } catch (IOException ex) {
            throw new CatalogueQueryException(ex.getMessage());
        }
    }


}

