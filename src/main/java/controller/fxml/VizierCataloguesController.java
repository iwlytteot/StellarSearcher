package controller.fxml;

import controller.http.vizier.VizierService;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import model.Catalogue;
import model.Data;
import model.Table;
import model.exception.CatalogueQueryException;
import model.exception.TimeoutQueryException;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;
import utils.FxmlCreator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class controller for "VizierCataloguesWindow.fxml".
 */
@Component
@FxmlView("/VizierCataloguesWindow.fxml")
@lombok.Data
@Slf4j
public class VizierCataloguesController {
    @FXML
    public Button addCatalogueVizierButton;
    @FXML
    public TextField inputVizierCatalogue;
    @FXML
    public TreeView<Data> treeView;

    private final HashMap<CheckBoxTreeItem<Data>, Stage> nodeFilters = new HashMap<>();
    private String vizierServer;

    public List<Catalogue> getSelectedCatalogues() {
        var output = new ArrayList<Catalogue>();
        if (treeView == null) {
            return output;
        }
        var tableFilters = getFilters();
        var node = treeView.getRoot();

        //Get sleected tables and sets columns from 'tableFilters'
        for (var itemCat : node.getChildren()) {
            if (((CheckBoxTreeItem<Data>) itemCat).isSelected()) {
                var tables = new ArrayList<Table>();
                for (var itemTab : itemCat.getChildren()) {
                    if (((CheckBoxTreeItem<Data>) itemTab).isSelected()) {
                        var table = new Table((Table) itemTab.getValue());
                        table.setColumns(tableFilters.getOrDefault(table.getName(), new HashMap<>()));
                        tables.add(table);
                    }
                }
                var catalogue = new Catalogue((Catalogue) itemCat.getValue());
                catalogue.setTables(tables);
                output.add(catalogue);
            }
        }
        return output;
    }

    private HashMap<String, HashMap<String, String>> getFilters() {
        var output = new HashMap<String, HashMap<String, String>>();
        nodeFilters.forEach((k, v) -> {
            if (k.isSelected()) {
                var controller = (FilterWindowController) v.getUserData();
                output.put((k.getValue()).getName(), controller.getConstraints());
            }
        });
        return output;
    }

    @FXML
    public void initialize () {
        CheckBoxTreeItem<Data> root = new CheckBoxTreeItem<>(new Catalogue());
        treeView.setRoot(root);
        treeView.setShowRoot(false);
        treeView.setCellFactory(CheckBoxTreeCell.forTreeView());
        treeView.setOnMouseClicked(eventHandler);
    }

    @FXML
    public void addCatalogueVizier() {
        if (inputVizierCatalogue.getText().isEmpty()) {
            dialoguePopup("Input is empty", Alert.AlertType.INFORMATION);
            return;
        }
        addCatalogueVizierButton.getScene().setCursor(Cursor.WAIT);
        addCatalogueVizierButton.setDisable(true);

        if (!catalogueRequestService.isRunning()) {
            catalogueRequestService.reset();
        }
        catalogueRequestService.start();
    }

    @FXML
    public void backVizier() {
        var stage = (Stage) addCatalogueVizierButton.getScene().getWindow();
        stage.hide();
    }

    private void dialoguePopup(String message, Alert.AlertType alertType) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle("Request failed");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * EventHandler that handles creating of Filter Window for respective Table
     */
    private final EventHandler<MouseEvent> eventHandler = new EventHandler<>() {
        @Override
        public void handle(MouseEvent event) {
            if (event.getClickCount() == 2) {
                var node = (CheckBoxTreeItem<Data>) treeView.getSelectionModel().getSelectedItem();
                if (node.getValue() instanceof Table) {
                    if (!nodeFilters.containsKey(node)) {
                        nodeFilters.putIfAbsent(node,
                                FxmlCreator.createFilterWindow(
                                        "Filter window - " + node.getValue().getName(),
                                        (Table) node.getValue()));
                    }
                    nodeFilters.get(node).show();
                }
            }
        }
    };

    /**
     * JavaFX Service, where search parameters are retrieved and where search action takes place.
     */
    private final Service<Void> catalogueRequestService = new Service<>() {
        @Override
        protected Task<Void> createTask() {
            return new Task<>() {
                @Override
                protected Void call() {
                    var requestService = new VizierService();
                    var request = requestService.createMetaDataRequest(inputVizierCatalogue.getText(),
                            vizierServer);
                    String result;
                    try {
                        result = requestService.sendRequest(request, false);
                    }
                    catch (TimeoutQueryException | CatalogueQueryException ex) {
                        dialoguePopup(ex.getMessage(), Alert.AlertType.ERROR);
                        log.error("Error during retrieving data: " + ex.getMessage());
                        return null;
                    }

                    if (result.isEmpty()) {
                        return null;
                    }
                    var catOutput = Catalogue.parseMetaData(result);
                    var catTemp = new ArrayList<CheckBoxTreeItem<Data>>();
                    for (var catalogue : catOutput) {
                        //If there already exists catalogue with the same name
                        if (treeView.getRoot().getChildren().stream().anyMatch(e -> e.getValue().getName().equals(catalogue.getName()))) {
                            continue;
                        }
                        CheckBoxTreeItem<Data> catalogueNode = new CheckBoxTreeItem<>(catalogue);
                        var temp = new ArrayList<CheckBoxTreeItem<Data>>();
                        catalogue.getTables().forEach(e -> temp.add(new CheckBoxTreeItem<>(e)));
                        catalogueNode.getChildren().addAll(temp);
                        catalogueNode.setSelected(true);
                        catTemp.add(catalogueNode);
                    }
                    Platform.runLater(() -> treeView.getRoot().getChildren().addAll(catTemp));
                    return null;
                }
            };
        }

        @Override
        protected void succeeded() {
            super.succeeded();
            addCatalogueVizierButton.getScene().setCursor(Cursor.DEFAULT);
            addCatalogueVizierButton.setDisable(false);

        }

        @Override
        protected void cancelled() {
            super.cancelled();
            dialoguePopup("Task was cancelled", Alert.AlertType.ERROR);
            addCatalogueVizierButton.getScene().setCursor(Cursor.DEFAULT);
            addCatalogueVizierButton.setDisable(false);
        }

        @Override
        protected void failed() {
            super.failed();
            dialoguePopup("Task failed.", Alert.AlertType.ERROR);
            addCatalogueVizierButton.getScene().setCursor(Cursor.DEFAULT);
            addCatalogueVizierButton.setDisable(false);
        }

    };

    public void addCataloguePress(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            addCatalogueVizier();
        }
    }
}
