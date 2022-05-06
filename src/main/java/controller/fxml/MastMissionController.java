package controller.fxml;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;
import model.Table;
import model.TableListCell;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;
import utils.FxmlCreator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class controller for "MastMissionWindow.fxml".
 */
@Controller
@FxmlView("/fxml/MastMissionWindow.fxml")
@Slf4j
public class MastMissionController {
    @FXML
    public ListView<Table> mastMissionList;

    private final HashMap<Table, ObservableValue<Boolean>> items = new HashMap<>();
    private final HashMap<Table, Stage> nodeFilters = new HashMap<>();
    private boolean setAll = true;

    /**
     * Method that initializes after FXML view is created. From directory "/mast_tables" retrieves
     * every file and parses them into Tables with acronyms for respective missions and catalogues.
     * Then saves the Table object into ListView for further inspecting.
     */
    @FXML
    public void initialize() {
        var tables = new ArrayList<Table>();
        try {
            ObjectMapper mapper = new ObjectMapper();

            var pathToMissions = getClass().getResource("/mast_tables");
            if (pathToMissions == null) {
                log.error("Path to mast_tables was not found");
                return;
            }
            var files = new File(pathToMissions.getPath()).listFiles();
            if (files == null) {
                log.error("Empty mast_tables folder");
                return;
            }
            for (File file : files) {
                if (file.getName().startsWith(".")) {
                    continue; //for macOS
                }
                var table = mapper.readValue(file, Table.class);
                tables.add(table);
            }
        } catch (Exception ex) {
            log.error("Error while processing \"/mast_tables\" into application: " + ex.getMessage());
        }

        //Initializes each table with SimpleBooleanProperty into HashMap, which is further
        //used in CellFactory of ListView
        for (var table : tables) {
            items.put(table, new SimpleBooleanProperty(false));
        }

        mastMissionList.setItems(FXCollections.observableList(new ArrayList<>(items.keySet())).sorted());
        Callback<Table, ObservableValue<Boolean>> callback = items::get;
        mastMissionList.setCellFactory(e -> new TableListCell(callback));
        mastMissionList.setOnMouseClicked(eventHandler);

    }

    @FXML
    public void backMast() {
        var stage = (Stage) mastMissionList.getScene().getWindow();
        stage.close();
    }

    /**
     * Selects or deselects all tables from list
     */
    @FXML
    public void mButtonAction() {
        for (var entry : items.entrySet()) {
            entry.setValue(new SimpleBooleanProperty(setAll));
        }
        setAll = !setAll;
        mastMissionList.setItems(FXCollections.observableList(new ArrayList<>(items.keySet())).sorted());
    }

    /**
     * Method that retrieves all selected (checkboxed) tables from ListView.
     * @return List of selected Tables
     */
    public List<Table> getSelectedMissions() {
        var output = new ArrayList<Table>();

        //Get all selected tables
        var selectedMissions = items.entrySet().stream()
                .filter(x -> x.getValue().getValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        //Get all constraints from respective table
        for (var mission : selectedMissions) {
            var outMission = new Table(mission);
            outMission.setColumns(new HashMap<>());
            if (nodeFilters.containsKey(mission)) {
                var stage = nodeFilters.get(mission);
                var controller = (FilterWindowController) stage.getUserData();
                var temp = controller.getConstraints();
                var constraints = new HashMap<String, String>();
                temp.forEach((k, v) -> constraints.put(mission.getColumns().get(k), v));
                outMission.setColumns(constraints);
            }
            output.add(outMission);
        }
        return output;
    }

    /**
     * Creates Filter window for a MAST mission. In these windows columns of table are shown.
     */
    private final EventHandler<MouseEvent> eventHandler = new EventHandler<>() {
        @Override
        public void handle(MouseEvent event) {
            if (event.getClickCount() == 2) {
                var node = mastMissionList.getSelectionModel().getSelectedItem();
                if (!nodeFilters.containsKey(node)) {
                    nodeFilters.putIfAbsent(node,
                            FxmlCreator.createFilterWindow(
                                    "Filter window - " + node.getName(),
                                    node));
                }
                nodeFilters.get(node).show();
            }
        }
    };

}
