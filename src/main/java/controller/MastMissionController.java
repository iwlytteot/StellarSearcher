package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Callback;
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

@Controller
@FxmlView("/MastMissionWindow.fxml")
public class MastMissionController {
    @FXML
    public ListView<Table> mastMissionList;

    private final HashMap<Table, ObservableValue<Boolean>> items = new HashMap<>();
    private final HashMap<Table, Stage> nodeFilters = new HashMap<>();
    private boolean setAll = true;

    @FXML
    public void initialize() {
        var tables = new ArrayList<Table>();
        try {
            ObjectMapper mapper = new ObjectMapper();

            String folderPath = new File("").getAbsolutePath() + File.separator + "mast_tables";

            for (File file : new File(folderPath).listFiles()) {
                var table = mapper.readValue(file, Table.class);
                tables.add(table);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        for (var table : tables) {
            items.put(table, new SimpleBooleanProperty(false));
        }

        mastMissionList.setItems(FXCollections.observableList(new ArrayList<>(items.keySet())).sorted());
        Callback<Table, ObservableValue<Boolean>> callback = items::get;
        mastMissionList.setCellFactory(e -> new TableListCell(callback));
        mastMissionList.setOnMouseClicked(eventHandler);

    }

    public void backMast() {
        var stage = (Stage) mastMissionList.getScene().getWindow();
        stage.close();
    }

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

    public void mButtonAction() {
        for (var entry : items.entrySet()) {
            entry.setValue(new SimpleBooleanProperty(setAll));
        }
        setAll = !setAll;
        Platform.runLater(() ->
                mastMissionList.setItems(FXCollections.observableList(new ArrayList<>(items.keySet())).sorted()));
    }

    public List<Table> getSelectedMissions() {
        var output = new ArrayList<Table>();
        var selectedMissions = items.entrySet().stream()
                .filter(x -> x.getValue().getValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
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

}
