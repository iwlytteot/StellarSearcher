package controller;

import controller.http.vizier.VizierRequest;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.Catalogue;
import model.CatalogueQueryException;
import model.Data;
import model.Table;
import utils.FxmlCreator;

import java.util.ArrayList;
import java.util.HashMap;

public class VizierCataloguesWindow {
    @FXML
    public Button addCatalogueVizierButton;
    @FXML
    public TextField inputVizierCatalogue;
    @FXML
    public TreeView<Data> treeView;

    private final HashMap<CheckBoxTreeItem<Data>, Stage> nodeFilters = new HashMap<>();

    public void init () {
        CheckBoxTreeItem<Data> root = new CheckBoxTreeItem<>(new Catalogue());
        treeView.setRoot(root);
        treeView.setShowRoot(false);
        treeView.setCellFactory(CheckBoxTreeCell.forTreeView());
        treeView.setOnMouseClicked(eventHandler);
    }

    public void addCatalogueVizier(ActionEvent actionEvent) {
        if (inputVizierCatalogue.getText().isEmpty()) {
            dialoguePopup("Input is empty", Alert.AlertType.INFORMATION);
            return;
        }
        Platform.runLater(() -> addCatalogueVizierButton.getScene().setCursor(Cursor.WAIT));
        Platform.runLater(() -> addCatalogueVizierButton.setDisable(true));

        if (!catalogueRequestService.isRunning()) {
            catalogueRequestService.reset();
        }
        catalogueRequestService.start();
    }

    public void backVizier(ActionEvent actionEvent) {
        var stage = (Stage) addCatalogueVizierButton.getScene().getWindow();
        stage.hide();
    }

    private static void dialoguePopup(String message, Alert.AlertType alertType) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle("Request failed");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private final EventHandler<MouseEvent> eventHandler = new EventHandler<>() {
        @Override
        public void handle(MouseEvent event) {
            if (event.getClickCount() == 2) {
                var node = (CheckBoxTreeItem<Data>) treeView.getSelectionModel().getSelectedItem();
                if (node.getValue() instanceof Table) {
                    if (!nodeFilters.containsKey(node)) {
                        nodeFilters.putIfAbsent(node,
                                FxmlCreator.create(
                                        "Filter window - " + node.getValue().getName(),
                                        (Table) node.getValue()));
                    }
                nodeFilters.get(node).show();
                }
            }
        }
    };


    private final Service<Void> catalogueRequestService = new Service<>() {
        @Override
        protected Task<Void> createTask() {
            return new Task<>() {
                @Override
                protected Void call() {
                    var requestService = new VizierRequest();
                    var request = requestService.createMetaDataRequest(inputVizierCatalogue.getText());
                    try {
                        requestService.sendRequest(request);
                    }
                    catch (CatalogueQueryException ex) {
                        dialoguePopup(ex.getMessage(), Alert.AlertType.ERROR);
                        return null;
                    }

                    var catOutput = Catalogue.parseMetaData("data.txt");
                    for (var catalogue : catOutput) {
                        if (treeView.getRoot().getChildren().stream().anyMatch(e -> e.getValue().getName().equals(catalogue.getName()))) {
                            dialoguePopup("There is already " + catalogue.getName() + " in list", Alert.AlertType.WARNING);
                            continue;
                        }
                        CheckBoxTreeItem<Data> catalogueNode = new CheckBoxTreeItem<>(catalogue);
                        var temp = new ArrayList<CheckBoxTreeItem<Data>>();
                        catalogue.getTables().forEach(e -> temp.add(new CheckBoxTreeItem<>(e)));
                        catalogueNode.getChildren().addAll(temp);
                        catalogueNode.setSelected(true);
                        Platform.runLater(() -> treeView.getRoot().getChildren().add(catalogueNode));
                    }
                    return null;
                }
            };
        }

        @Override
        protected void succeeded() {
            super.succeeded();
            Platform.runLater(() -> addCatalogueVizierButton.getScene().setCursor(Cursor.DEFAULT));
            Platform.runLater(() -> addCatalogueVizierButton.setDisable(false));

        }

        @Override
        protected void cancelled() {
            super.cancelled();
            dialoguePopup("Task was cancelled", Alert.AlertType.ERROR);
            Platform.runLater(() -> addCatalogueVizierButton.getScene().setCursor(Cursor.DEFAULT));
            Platform.runLater(() -> addCatalogueVizierButton.setDisable(false));
        }

        @Override
        protected void failed() {
            super.failed();
            dialoguePopup("Task hasn't been finished. It is probably due to output limit/overload.", Alert.AlertType.ERROR);
            Platform.runLater(() -> addCatalogueVizierButton.getScene().setCursor(Cursor.DEFAULT));
            Platform.runLater(() -> addCatalogueVizierButton.setDisable(false));
        }

    };
}
