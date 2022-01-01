package controller;

import cds.savot.model.*;
import cds.savot.pull.SavotPullEngine;
import cds.savot.pull.SavotPullParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import utils.DataExporter;
import utils.FxmlCreator;

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
            if (resources.getItemCount() == 0) {
                return;
            }
            for (var item : resources.getItems()) {
                var s = (SavotResource) item;
                var tables = s.getTables();
                for (var table : tables.getItems()) {
                    var t = (SavotTable) table;
                    var tableTab = new Tab(t.getName());

                    var tableView = new TableView<SavotTR>();
                    for (int i = 0; i < t.getFields().getItemCount(); ++i) {
                        var field = (SavotField) t.getFields().getItemAt(i);
                        var column = new TableColumn<SavotTR, String>(field.getDescription());
                        int finalI = i;
                        column.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().getTDs().getContent(finalI)));
                        column.setSortable(false);
                        tableView.getColumns().add(column);
                    }

                    if (t.getData() == null) {
                        continue;
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


        for (var tab : tabPane.getTabs()) {
            var p = (TableView<SavotTR>) tab.getContent();
            for (var item : p.getColumns()) {
                //System.out.println(item.getText()); // column name
            }
            System.out.println("h");
            for (var item : p.getItems()) {
                for (var f : item.getTDSet().getItems()) {
                    //System.out.println(((SavotTD) f).getContent()); // column value
                }
            }
        }

    }

    public void exportData(ActionEvent actionEvent) throws JsonProcessingException {
        var fxml = FxmlCreator.initFxml("/ExportWindow.fxml", "Export window", false);
        ExportWindowController exportWindowController = fxml.getFirst().getController();
        exportWindowController.setProceed(false);
        fxml.getSecond().showAndWait();
        if (!exportWindowController.isProceed() || exportWindowController.getSelectedDirectory() == null) {
            return;
        }


        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("DataSerial", new Version(1, 0, 0, null, null, null));
        for (var tab : tabPane.getTabs()) {

        }
        if (exportWindowController.getMergeCheckBox().isSelected()) {

        }
        var tab = tabPane.getTabs().get(0);
        module.addSerializer(Tab.class, new DataExporter());
        mapper.registerModule(module);

       System.out.println(mapper.writeValueAsString(tab));



    }
}
