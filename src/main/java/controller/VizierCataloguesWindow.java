package controller;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
    public ListView<String> tableVizierList;

    public void init() {
        tableVizierList.getItems().addAll("I/246", "II/246");
    }

    public void addCatalogueVizier(ActionEvent actionEvent) {
        Platform.runLater(() -> addCatalogueVizierButton.getScene().setCursor(Cursor.WAIT));

        if (!catalogueRequestService.isRunning()) {
            catalogueRequestService.reset();
        }
        catalogueRequestService.start();
    }

    public void backVizier(ActionEvent actionEvent) {
        var stage = (Stage) addCatalogueVizierButton.getScene().getWindow();
        stage.close();
    }


    private final Service<Void> catalogueRequestService= new Service<>() {
        @Override
        protected Task<Void> createTask() {
            return new Task<>() {
                @Override
                protected Void call() {
                    var client = HttpClient.newHttpClient();
                    var request = HttpRequest
                            .newBuilder(URI.create("https://vizier.u-strasbg.fr/viz-bin/asu-tsv?-source="
                                    + inputVizierCatalogue.getText() +
                                    "&-meta.all&-meta.max=30"))
                            .GET()
                            .build();
                    try {
                        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        FileWriter myWriter = new FileWriter("catGetVizier.txt");
                        myWriter.write(response.body());
                        myWriter.close();

                    } catch (IOException | InterruptedException e) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Request failed");
                            alert.setContentText(e.getMessage());
                            alert.showAndWait();
                        });
                    }
                    return null;
                }
            };
        }

        @Override
        protected void succeeded() {
            super.succeeded();
            Platform.runLater(() -> addCatalogueVizierButton.getScene().setCursor(Cursor.DEFAULT));
        }

        @Override
        protected void cancelled() {
            super.cancelled();
            Platform.runLater(() -> addCatalogueVizierButton.getScene().setCursor(Cursor.DEFAULT));
        }

        @Override
        protected void failed() {
            super.failed();
            Platform.runLater(() -> addCatalogueVizierButton.getScene().setCursor(Cursor.DEFAULT));
        }
    };
}
