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
import javafx.stage.Stage;
import model.DataWriteException;
import model.OutputData;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import utils.DataExporter;
import view.event.ExportWindowEvent;
import view.handler.ExportWindowEventHandler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Component
@FxmlView("/ResultWindow.fxml")
public class ResultWindowController {
    private final ConfigurableApplicationContext context;
    private final ExportWindowEventHandler exportWindowEventHandler;
    private final ExportWindowController exportWindowController;
    private final OutputData outputData;
    @FXML
    public TabPane tabPane;

    public ResultWindowController(ConfigurableApplicationContext context, ExportWindowEventHandler exportWindowEventHandler, ExportWindowController exportWindowController, OutputData outputData) {
        this.context = context;
        this.exportWindowEventHandler = exportWindowEventHandler;
        this.exportWindowController = exportWindowController;
        this.outputData = outputData;
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
        if (exportWindowEventHandler.getStage() == null) {
            context.publishEvent(new ExportWindowEvent(new Stage()));
        }
        exportWindowController.setProceed(false);
        exportWindowEventHandler.getStage().showAndWait();
        if (!exportWindowController.isProceed() || exportWindowController.getSelectedDirectory() == null) {
            return;
        }


        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("DataSerial", new Version(1, 0, 0, null, null, null));
        module.addSerializer(OutputData.class, new DataExporter());
        mapper.registerModule(module);

        try {
            for (var tab : tabPane.getTabs()) {
                FileWriter myWriter = new FileWriter(exportWindowController.getSelectedDirectory().getAbsolutePath() + "/" + tab.getText().replace("/", "_") + ".txt");
                outputData.setTab(tab);
                myWriter.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(outputData));
                myWriter.close();
            }
        } catch (IOException e) {
            throw new DataWriteException(e.getMessage());
        }
    }
}
