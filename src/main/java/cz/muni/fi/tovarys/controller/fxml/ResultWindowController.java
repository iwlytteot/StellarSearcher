package cz.muni.fi.tovarys.controller.fxml;

import cds.savot.model.*;
import cds.savot.pull.SavotPullEngine;
import cds.savot.pull.SavotPullParser;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import cz.muni.fi.tovarys.model.UserInput;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import cz.muni.fi.tovarys.view.event.ExportWindowEvent;
import cz.muni.fi.tovarys.view.handler.ExportWindowEventHandler;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

/**
 * Class controller for "ResultWindow.fxml".
 */
@Component
@FxmlView("/fxml/ResultWindow.fxml")
@Data
@Slf4j
public class ResultWindowController {
    @FXML
    public TabPane tabPane;
    @FXML
    public Label numRows;

    private final ConfigurableApplicationContext context;
    private final ExportWindowEventHandler exportWindowEventHandler;

    /**
     * Method that opens the directory chooser and then proceeds to export data such that file name is enumerated as
     * 'x.txt', where x is in range <0, p> and 'p' is number Tabs to be exported.
     */
    @FXML
    public void exportData() {
        if (exportWindowEventHandler.getStage() == null) {
            context.publishEvent(new ExportWindowEvent(new Stage()));
        }
        exportWindowEventHandler.getStage().show();
    }

    /**
     * Fills TabPane with Tabs. These tabs are results from searching in servers.
     * @param output result from searching
     * @param numOfCols number of output columns
     * @param numOfRows number of output rows
     */

    public void fill(HashMap<UserInput, List<String>> output, int numOfCols, int numOfRows) {
        int rowCount = 0;
        for (var entry : output.entrySet()) {
            HashMap<String, TableView<SavotTR>> outputTableViews = new HashMap<>();
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
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("There has been an error during retrieving some data");
                    alert.setContentText(stringBuilder.toString());
                    alert.showAndWait();
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

                        var tableView = new TableView<SavotTR>();
                        var columnCount = 0;
                        for (int i = 0; i < t.getFields().getItemCount(); ++i) {
                            var field = (SavotField) t.getFields().getItemAt(i);
                            var column = new TableColumn<SavotTR, String>(field.getDescription());
                            int finalI = i;
                            column.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().getTDs().getContent(finalI)));
                            column.setSortable(false);
                            tableView.getColumns().add(column);
                            if (numOfCols != -1 && columnCount >= numOfCols) {
                                column.setVisible(false);
                            } else {
                                ++columnCount;
                            }
                        }

                        if (t.getData() == null) {
                            continue;
                        }
                        var data = t.getData().getTableData();
                        if (numOfRows == -1) {
                            rowCount += data.getTRs().getItemCount();
                        }
                        else {
                            rowCount += Math.min(numOfRows, data.getTRs().getItemCount());
                        }
                        int tableRowCount = 0;
                        if (data.getTRs().getItems() != null) {
                            for (var trItem : data.getTRs().getItems()) {
                                var tr = (SavotTR) trItem;
                                if (numOfRows == -1 || tableRowCount < numOfRows) {
                                    tableView.getItems().add(tr);
                                    ++tableRowCount;
                                }
                            }
                        }
                        if (!tableView.getItems().isEmpty()) {
                            if (outputTableViews.containsKey(t.getName())) {
                                outputTableViews.get(t.getName()).getItems().addAll(tableView.getItems());
                            } else {
                                outputTableViews.put(t.getName(), tableView);
                            }
                        }
                    }
                }
            }
            for (var tableView : outputTableViews.entrySet()) {
                var tab = new Tab(tableView.getKey());
                tab.setContent(tableView.getValue());
                inputPane.getTabs().add(tab);
            }
            inputTab.setContent(inputPane);
            tabPane.getTabs().add(inputTab);
        }
        numRows.setText("Fetched " + rowCount + " row(s)");
    }
}
