package controller;

import cds.savot.model.*;
import cds.savot.pull.SavotPullEngine;
import cds.savot.pull.SavotPullParser;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class ResultWindowController {
    @FXML
    public TabPane tabPane;

    public void init() {
    }

    public void fill(List<String> affectedTables) {
        tabPane.getTabs().clear();

        for (var tableName : affectedTables) {
            SavotPullParser sb = new SavotPullParser("data/" + tableName + ".txt", SavotPullEngine.FULL);
            SavotVOTable sv = sb.getVOTable();
            StringBuilder stringBuilder = new StringBuilder();
            if (sv.getInfos() != null && sv.getInfos().getItems() != null) {
                for (var infoItem : sv.getInfos().getItems()) {
                    var info = (SavotInfo) infoItem;
                    if (info.getName().equals("Error")) {
                        stringBuilder.append(info.getValue()).append(" ");
                    }
                }
            }
            if (!stringBuilder.isEmpty()) {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("There has been an error during retrieving some data");
                    alert.setContentText(stringBuilder.toString());
                    alert.showAndWait();
                });
                continue;
            }
            var resources = sv.getResources();
            for (var item : resources.getItems()) {
                var s = (SavotResource) item;
                var tables = s.getTables();
                for (var table : tables.getItems()) {
                    var t = (SavotTable) table;
                    var tableTab = new Tab(t.getName());

                    var tableView = new TableView<SavotTR>();
                    for (int i = 0; i < t.getFields().getItemCount(); ++i) {
                        var field = (SavotField) t.getFields().getItemAt(i);
                        var column = new TableColumn<SavotTR, String>(field.getName());
                        int finalI = i;
                        column.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().getTDs().getContent(finalI)));
                        column.setSortable(false);
                        tableView.getColumns().add(column);
                    }

                    var data = t.getData().getTableData();
                    if (data.getTRs().getItems() != null) {
                        for (var trItem : data.getTRs().getItems()) {
                            var tr = (SavotTR) trItem;
                            tableView.getItems().add(tr);
                        }
                    }
                    if (!tableView.getItems().isEmpty()) {
                        tableTab.setContent(tableView);
                        tabPane.getTabs().add(tableTab);
                    }
                }
            }
        }

    }

}
