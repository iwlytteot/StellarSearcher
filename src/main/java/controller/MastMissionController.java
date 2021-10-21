package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Table;
import model.TableListCell;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class MastMissionController {
    @FXML
    public ListView<Table> mastMissionList;

    private final HashMap<Table, ObservableValue<Boolean>> items = new HashMap<>();

    public void init() {
        var tables = new ArrayList<Table>();
        try {
            ObjectMapper mapper = new ObjectMapper();

            String folderPath = "C:\\Users\\realwayt\\IdeaProjects\\thesis-leotovarys\\mast_tables";
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

    }

    public void backMast(ActionEvent actionEvent) {
        var stage = (Stage) mastMissionList.getScene().getWindow();
        stage.close();
    }
}
