package controller;

import cds.savot.model.*;
import cds.savot.pull.SavotPullEngine;
import cds.savot.pull.SavotPullParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import model.OutputData;
import model.UserInput;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import utils.DataExporter;
import view.event.ExportWindowEvent;
import view.handler.ExportWindowEventHandler;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class controller for "ResultWindow.fxml".
 */
@Component
@FxmlView("/ResultWindow.fxml")
@Data
@Slf4j
public class ResultWindowController {
    private final ConfigurableApplicationContext context;
    private final ExportWindowEventHandler exportWindowEventHandler;
    private final ExportWindowController exportWindowController;
    @FXML
    public TabPane tabPane;

    /**
     * Fills TabPane with Tabs. These tabs are results from searching in servers.
     * @param output result from searching
     */
    public void fill(HashMap<UserInput, List<String>> output) {
        //If there was multiple searching
        tabPane.getTabs().clear();

        for (var entry : output.entrySet()) {
            //Retrieves input and radius and creates Tab
            var inputTab = new Tab(entry.getKey().toString());
            var inputPane = new TabPane();

            //For every result in respective input in VOTable format
            for (var singleTable : entry.getValue()) {
                SavotPullParser sb = new SavotPullParser(new ByteArrayInputStream(singleTable.getBytes(StandardCharsets.UTF_8)), SavotPullEngine.FULL, "UTF-8");
                SavotVOTable sv = sb.getVOTable();
                StringBuilder stringBuilder = new StringBuilder();

                //Part where we look for errors during parsing
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

                //Start of converting ResourceSet (from SAVOT framework) to Tab. That means
                //setting CellValueFactory for columns and setting content for rows
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

    /**
     * Method that opens the directory chooser and then proceeds to export data such that file name is enumerated as
     * 'x.txt', where x is in range <0, p>, where 'p' is number of exported Tabs.
     */
    public void exportData() {
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

        //Mapping Tab object into JSON object via ObjectMapper that uses DataExporter as its map.
        int i = 0;
        for (var inputTab : tabPane.getTabs()) {
            var input = Arrays.stream(inputTab.getText().split(";")).collect(Collectors.toList());
            for (var tab : ((TabPane) inputTab.getContent()).getTabs()) {
                try {
                    FileWriter myWriter = new FileWriter(exportWindowController.getSelectedDirectory().getAbsolutePath() + "/" + i + ".txt");
                    var outputData = new OutputData(input.get(0), input.get(1));
                    outputData.setTab(tab);
                    myWriter.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(outputData));
                    myWriter.close();
                } catch (IOException e) {
                    log.error("Error during exporting: " + e.getMessage());
                }
                ++i;
            }
        }
    }
}
