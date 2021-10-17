package controller;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Catalogue;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class VizierCataloguesWindow {
    @FXML
    public Button addCatalogueVizierButton;
    @FXML
    public TextField inputVizierCatalogue;
    @FXML
    public ListView<Catalogue> tableVizierList;
    @FXML
    public TextField maxCatResultInput;
    @FXML
    public Label invalidInputLabel;

    public void addCatalogueVizier(ActionEvent actionEvent) {
        var input = NumberUtils.toInt(maxCatResultInput.getText());
        if (input <= 0) {
            invalidInputLabel.setText("Check your input");
            return;
        } else {
            invalidInputLabel.setText("");
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
                                    "&-meta.all&-meta.max="
                                    + maxCatResultInput.getText()))
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
                    Platform.runLater(() -> tableVizierList.getItems().addAll(catOutput));
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
