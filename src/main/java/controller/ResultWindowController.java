package controller;

import cds.savot.model.*;
import cds.savot.pull.SavotPullEngine;
import cds.savot.pull.SavotPullParser;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ResultWindowController {
    @FXML
    public TabPane tabPane;

    public void init() {
        SavotPullParser sb = new SavotPullParser("vizier_data.txt", SavotPullEngine.FULL);
        SavotVOTable sv = sb.getVOTable();
        StringBuilder stringBuilder = new StringBuilder();
        boolean errorOccured = false;
        for (var infoItem : sv.getInfos().getItems()) {
            var info = (SavotInfo) infoItem;
            if (info.getName().equals("Error")) {
                errorOccured = true;
                stringBuilder.append(info.getValue()).append(" ");
            }
        }
        if (errorOccured) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("There has been an error during retrieving some data");
                alert.setContentText(stringBuilder.toString());
                alert.showAndWait();
            });
            return;
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
                for (var trItem : data.getTRs().getItems()) {
                    var tr = (SavotTR) trItem;
                    tableView.getItems().add(tr);
                }
                tableTab.setContent(tableView);
                tabPane.getTabs().add(tableTab);
            }
        }
    }

}
