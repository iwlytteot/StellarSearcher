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
import model.UserInput;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import utils.DataExporter;
import view.event.ExportWindowEvent;
import view.handler.ExportWindowEventHandler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
@FxmlView("/ResultWindow.fxml")
public class ResultWindowController {
    private final ConfigurableApplicationContext context;
    private final ExportWindowEventHandler exportWindowEventHandler;
    private final ExportWindowController exportWindowController;
    @FXML
    public TabPane tabPane;

    public ResultWindowController(ConfigurableApplicationContext context, ExportWindowEventHandler exportWindowEventHandler, ExportWindowController exportWindowController) {
        this.context = context;
        this.exportWindowEventHandler = exportWindowEventHandler;
        this.exportWindowController = exportWindowController;
    }

    public void fill(HashMap<UserInput, List<String>> output) {
        tabPane.getTabs().clear();

        for (var entry : output.entrySet()) {
            var inputTab = new Tab(entry.getKey().toString());
            var inputPane = new TabPane();
            for (var singleTable : entry.getValue()) {
                SavotPullParser sb = new SavotPullParser(new ByteArrayInputStream(singleTable.getBytes(StandardCharsets.UTF_8)), SavotPullEngine.FULL, "UTF-8");
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
                            inputPane.getTabs().add(tableTab);
                        }
                    }
                }
            }
            inputTab.setContent(inputPane);
            tabPane.getTabs().add(inputTab);
        }
    }

    public void exportData(ActionEvent actionEvent) throws IOException {
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

        int i = 0;
        for (var inputTab : tabPane.getTabs()) {
            var input = Arrays.stream(inputTab.getText().split(";")).collect(Collectors.toList());
            for (var tab : ((TabPane) inputTab.getContent()).getTabs()) {
                FileWriter myWriter = new FileWriter(exportWindowController.getSelectedDirectory().getAbsolutePath() + "/" + i + ".txt");
                var outputData = new OutputData(input.get(0), input.get(1));
                outputData.setTab(tab);
                myWriter.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(outputData));
                myWriter.close();
                ++i;
            }
        }
    }
}
