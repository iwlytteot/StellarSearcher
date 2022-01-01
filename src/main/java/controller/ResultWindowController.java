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
import model.CatalogueQueryException;
import model.DataWriteException;
import utils.DataExporter;
import utils.FxmlCreator;

import java.io.FileWriter;
import java.io.IOException;
import java.net.http.HttpResponse;
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
        module.addSerializer(Tab.class, new DataExporter());
        mapper.registerModule(module);

        if (exportWindowController.getMergeCheckBox().isSelected()) {
            try {
                FileWriter myWriter = new FileWriter( exportWindowController.getSelectedDirectory().getAbsolutePath() + "/merged_data.txt");
                for (var tab : tabPane.getTabs()) {
                    myWriter.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(tab));
                }
                myWriter.close();

            } catch (IOException e) {
                throw new DataWriteException(e.getMessage());
            }
        }
        else {
            try {
                for (var tab : tabPane.getTabs()) {
                    FileWriter myWriter = new FileWriter(exportWindowController.getSelectedDirectory().getAbsolutePath() + "/" + tab.getText().replace("/", "_") + ".txt");
                    myWriter.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(tab));
                    myWriter.close();
                }
            } catch (IOException e) {
                throw new DataWriteException(e.getMessage());
            }
        }
    }
}
