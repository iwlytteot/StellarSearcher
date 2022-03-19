package controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.Table;

import java.io.IOException;
import java.util.HashMap;
import java.util.stream.Collectors;

public class FilterWindowController {
    @FXML
    public ListView<String> listViewAvailable;
    @FXML
    public ListView<String> listViewUsed;

    private final HashMap<String, Stage> constraints = new HashMap<>();

    public void init(Table table) {
        listViewAvailable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listViewUsed.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listViewAvailable.setOnMouseClicked(eventHandlerAvailable);
        listViewUsed.setOnMouseClicked(eventHandlerUsed);

        listViewAvailable.getItems().addAll(table.getColumns().keySet().stream().sorted().collect(Collectors.toList()));
    }

    public HashMap<String, String> getConstraints() {
        var output = new HashMap<String, String>();
        for (var item : listViewUsed.getItems()) {
            var controller = (ConstraintWindowController) constraints.get(item).getUserData();
            output.put(item, controller.getConstraint());
        }
        return output;
    }

    @FXML
    public void addFilter() {
        var selected = listViewAvailable.getSelectionModel().getSelectedItems();
        listViewUsed.getItems().addAll(selected);
        listViewAvailable.getItems().removeAll(selected);
    }

    @FXML
    public void removeFilter() {
        var selected = listViewUsed.getSelectionModel().getSelectedItems();
        listViewAvailable.getItems().addAll(selected);
        listViewUsed.getItems().removeAll(selected);
    }

    @FXML
    public void applyFilter() {
        var stage = (Stage) listViewAvailable.getScene().getWindow();
        stage.hide();
    }

    private final EventHandler<MouseEvent> eventHandlerAvailable = new EventHandler<>() {
        @Override
        public void handle(MouseEvent event) {
            if (event.getClickCount() == 2) {
                var selected = listViewAvailable.getSelectionModel().getSelectedItem();
                constraints.putIfAbsent(selected, initFxml(selected));
                constraints.get(selected).show();
            }
        }
    };

    private final EventHandler<MouseEvent> eventHandlerUsed = new EventHandler<>() {
        @Override
        public void handle(MouseEvent event) {
            if (event.getClickCount() == 2) {
                var selected = listViewUsed.getSelectionModel().getSelectedItem();
                constraints.putIfAbsent(selected, initFxml(selected));
                constraints.get(selected).show();
            }
        }
    };

    private Stage initFxml(String constraintName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ConstraintWindow.fxml"));
            Parent root = loader.load();

            ConstraintWindowController constraintWindowController = loader.getController();
            constraintWindowController.init(constraintName);

            Scene scene = new Scene(root);
            Stage stage = new Stage();

            stage.setUserData(constraintWindowController);
            stage.setScene(scene);
            stage.setTitle("Constraint window");
            return stage;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
