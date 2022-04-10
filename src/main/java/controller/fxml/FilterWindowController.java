package controller.fxml;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.Table;

import java.util.HashMap;
import java.util.stream.Collectors;

import static utils.FxmlCreator.createConstraintWindow;

/**
 * Class controller for "FilterWindow.fxml".
 */
public class FilterWindowController {
    @FXML
    public ListView<String> listViewAvailable;
    @FXML
    public ListView<String> listViewUsed;

    private final HashMap<String, Stage> constraints = new HashMap<>();

    /**
     * Switches selected constraint from Available to Used filters.
     */
    @FXML
    public void addFilter() {
        var selected = listViewAvailable.getSelectionModel().getSelectedItems();
        listViewUsed.getItems().addAll(selected);
        listViewAvailable.getItems().removeAll(selected);
    }

    /**
     * Switches selected constraint from Used to Available filters.
     */
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

    /**
     * Initializes ListView such that user can choose available filters and open its relative Constraint Window.
     * @param table Table object, where constraints (columns) are saved.
     */
    public void init(Table table) {
        listViewAvailable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listViewUsed.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listViewAvailable.setOnMouseClicked(eventHandlerAvailable);
        listViewUsed.setOnMouseClicked(eventHandlerUsed);

        listViewAvailable.getItems().addAll(table.getColumns().keySet().stream().sorted().collect(Collectors.toList()));
    }

    /**
     * Gets controller from Stage of respective Constraint Window and then retrieves constraints.
     * @return HashMap of String:String, where key is constraint and value is the value of constraint.
     */
    public HashMap<String, String> getConstraints() {
        var output = new HashMap<String, String>();
        for (var item : listViewUsed.getItems()) {
            var controller = (ConstraintWindowController) constraints.get(item).getUserData();
            output.put(item, controller.getConstraint());
        }
        return output;
    }

    /**
     * Creates and opens new window for selected constraint in Available
     */
    private final EventHandler<MouseEvent> eventHandlerAvailable = new EventHandler<>() {
        @Override
        public void handle(MouseEvent event) {
            if (event.getClickCount() == 2) {
                var selected = listViewAvailable.getSelectionModel().getSelectedItem();
                constraints.putIfAbsent(selected, createConstraintWindow(selected));
                constraints.get(selected).show();
            }
        }
    };

    /**
     * Creates and opens new window for selected constraint in Used
     */
    private final EventHandler<MouseEvent> eventHandlerUsed = new EventHandler<>() {
        @Override
        public void handle(MouseEvent event) {
            if (event.getClickCount() == 2) {
                var selected = listViewUsed.getSelectionModel().getSelectedItem();
                constraints.putIfAbsent(selected, createConstraintWindow(selected));
                constraints.get(selected).show();
            }
        }
    };
}
