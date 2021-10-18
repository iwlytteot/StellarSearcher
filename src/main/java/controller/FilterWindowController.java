package controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import model.Table;

public class FilterWindowController {
    @FXML
    public ListView<String> listViewAvailable;
    @FXML
    public ListView<String> listViewUsed;

    public void constraintWindowAction(MouseEvent mouseEvent) {
    }

    public void init(Table table) {
        listViewAvailable.getItems().addAll(table.getColumns().keySet());
    }
}
