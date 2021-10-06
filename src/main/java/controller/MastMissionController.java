package controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.CatalogueListCell;

import java.util.ArrayList;
import java.util.HashMap;

public class MastMissionController {
    @FXML
    public ListView<String> mastMissionList;

    private final HashMap<String, ObservableValue<Boolean>> items = new HashMap<>();

    public void init() {
        items.put("HST", new SimpleBooleanProperty(false));
        items.put("kepler", new SimpleBooleanProperty(false));
        items.put("k2", new SimpleBooleanProperty(false));

        mastMissionList.setItems(FXCollections.observableList(new ArrayList<>(items.keySet())).sorted());
        Callback<String, ObservableValue<Boolean>> keyToValue = items::get;
        mastMissionList.setCellFactory(e -> new CatalogueListCell(keyToValue));

    }

    public void backMast(ActionEvent actionEvent) {
        var stage = (Stage) mastMissionList.getScene().getWindow();
        stage.close();
    }
}
