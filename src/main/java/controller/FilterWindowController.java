package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
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
        listViewAvailable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listViewUsed.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        listViewAvailable.getItems().addAll(table.getColumns().keySet());
    }

    @FXML
    public void addFilter(ActionEvent actionEvent) {
        var selected = listViewAvailable.getSelectionModel().getSelectedItems();
        listViewUsed.getItems().addAll(selected);
        listViewAvailable.getItems().removeAll(selected);
    }

    @FXML
    public void removeFilter(ActionEvent actionEvent) {
        var selected = listViewUsed.getSelectionModel().getSelectedItems();
        listViewAvailable.getItems().addAll(selected);
        listViewUsed.getItems().removeAll(selected);
    }
}
