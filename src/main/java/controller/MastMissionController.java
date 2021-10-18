package controller;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.HashMap;

public class MastMissionController {
    @FXML
    public ListView<String> mastMissionList;

    private final HashMap<String, ObservableValue<Boolean>> items = new HashMap<>();


    public void backMast(ActionEvent actionEvent) {
        var stage = (Stage) mastMissionList.getScene().getWindow();
        stage.close();
    }
}
