package controller;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.stage.Stage;
import model.Catalogue;
import model.Data;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

public class VizierCataloguesWindow {
    @FXML
    public Button addCatalogueVizierButton;
    @FXML
    public TextField inputVizierCatalogue;
    @FXML
    public TreeView<Data> treeView;

    public void init () {
        CheckBoxTreeItem<Data> root = new CheckBoxTreeItem<>(new Catalogue());
        treeView.setRoot(root);
        treeView.setShowRoot(false);
        treeView.setCellFactory(CheckBoxTreeCell.forTreeView());
    }

    public void addCatalogueVizier(ActionEvent actionEvent) {
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

    private static void dialoguePopup(Exception e) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Request failed");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        });
    }


    private final Service<Void> catalogueRequestService = new Service<>() {
        @Override
        protected Task<Void> createTask() {
            return new Task<>() {
                @Override
                protected Void call() {
                    var client = HttpClient.newHttpClient();
                    var request = HttpRequest
                            .newBuilder(URI.create("https://vizier.u-strasbg.fr/viz-bin/asu-tsv?-source="
                                    + inputVizierCatalogue.getText() +
                                    "&-meta.all&-meta.max=500"))
                            .GET()
                            .build();
                    try {
                        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        FileWriter myWriter = new FileWriter("catGetVizier.txt");
                        myWriter.write(response.body());
                        myWriter.close();

                    } catch (IOException | InterruptedException e) {
                        dialoguePopup(e);
                    }
                    var catOutput = Catalogue.parseMetaData("catGetVizier.txt");
                    for (var catalogue : catOutput) {
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
            dialoguePopup(new Exception("Task was cancelled"));
            Platform.runLater(() -> addCatalogueVizierButton.getScene().setCursor(Cursor.DEFAULT));
            Platform.runLater(() -> addCatalogueVizierButton.setDisable(false));
        }

        @Override
        protected void failed() {
            super.failed();
            dialoguePopup(new Exception("Task has failed"));
            Platform.runLater(() -> addCatalogueVizierButton.getScene().setCursor(Cursor.DEFAULT));
            Platform.runLater(() -> addCatalogueVizierButton.setDisable(false));
        }

    };
}
