package controller;

import cds.savot.model.SavotField;
import cds.savot.model.SavotResource;
import cds.savot.model.SavotTable;
import cds.savot.model.SavotVOTable;
import cds.savot.pull.SavotPullEngine;
import cds.savot.pull.SavotPullParser;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ResultWindowController {
    @FXML
    public TabPane tabPane;

    public void init() {
        SavotPullParser sb = new SavotPullParser("vizier_data.txt", SavotPullEngine.FULL);
        SavotVOTable sv = sb.getVOTable();
        var resources = sv.getResources();
        for (var item : resources.getItems()) {
            var s = (SavotResource) item;
            System.out.println("cat: " + s.getName());
            var tables = s.getTables();
            for (var table : tables.getItems()) {
                var t = (SavotTable) table;
                var tableTab = new Tab(t.getName());

                var tableView = new TableView<>();
                for (int i = 0; i < t.getFields().getItemCount(); ++i) {
                    var field = (SavotField) t.getFields().getItemAt(i);
                    tableView.getColumns().add(new TableColumn<>(field.getName()));
                }
                tableTab.setContent(tableView);
                tabPane.getTabs().add(tableTab);
            }
        }
    }

}
