package controller;

import controller.http.mast.MastRequest;
import controller.http.mast.MastService;
import controller.http.vizier.VizierService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Catalogue;
import model.Radius;
import utils.Tuple;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainWindowController {
    @FXML
    public Rectangle rectLeft;
    @FXML
    public Rectangle rectMid;
    @FXML
    public Rectangle rectRight;
    @FXML
    public Button vizierButton;
    @FXML
    public Button simbadButton;
    @FXML
    public Button mastButton;
    @FXML
    public ComboBox<Radius> radiusBox;
    @FXML
    public Button mastTableButton;
    @FXML
    public Button vizierTableButton;
    @FXML
    public Button searchButton;
    @FXML
    public TextField inputText;
    @FXML
    public TextField radiusInput;

    private boolean vizierSearch = false, simbadSearch = false, mastSearch = false;
    private Stage vizierStage;
    private Stage mastStage;
    private FXMLLoader vizierLoader;
    private FXMLLoader mastLoader;

    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final ExecutorService executorWrapper = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final ExecutorCompletionService<Void> executorCompletionService = new ExecutorCompletionService<>(executorWrapper);

    public void init() {
        var vizierFXML = initFxml("/VizierCataloguesWindow.fxml", "Vizier catalogues");
        if (vizierFXML != null) {
            vizierLoader = vizierFXML.getFirst();
            vizierStage = vizierFXML.getSecond();

        }
        var mastFXML = initFxml("/MastMissionWindow.fxml", "MAST missions");
        if (mastFXML != null) {
            mastLoader = mastFXML.getFirst();
            mastStage = mastFXML.getSecond();
        }

        radiusBox.getItems().setAll(Radius.values());
        radiusBox.getSelectionModel().selectFirst();

        vizierTableButton.setDisable(true);
        mastTableButton.setDisable(true);
        searchButton.setDisable(true);

    }

    public void enterVizier(MouseEvent mouseEvent) {
        vizierSearch = !vizierSearch;
        rectLeft.setFill(vizierSearch ? Color.GREEN : Color.RED);
        vizierTableButton.setDisable(!vizierSearch);
        SearchButtonCheck();
    }

    public void enterSimbad(MouseEvent mouseEvent) {
        simbadSearch = !simbadSearch;
        rectMid.setFill(simbadSearch ? Color.GREEN : Color.RED);
        SearchButtonCheck();
    }

    public void enterMast(MouseEvent mouseEvent) {
        mastSearch = !mastSearch;
        rectRight.setFill(mastSearch ? Color.GREEN : Color.RED);
        mastTableButton.setDisable(!mastSearch);
        SearchButtonCheck();
    }

    public void mastTableButtonAction(ActionEvent actionEvent) {
        mastStage.show();
    }

    public void vizierTableButtonAction(ActionEvent actionEvent) {
        vizierStage.show();
    }

    private void SearchButtonCheck() {
        searchButton.setDisable(!mastSearch && !simbadSearch && !vizierSearch);
    }

    private Tuple<FXMLLoader, Stage> initFxml(String resourcePath, String stageTitle) {
        try {
            FXMLLoader loader = new FXMLLoader(MainWindowController.class.getResource(resourcePath));
            Parent root = loader.load();

            Method initMethod = Class.forName(loader.getController().getClass().getName()).getMethod("init");
            initMethod.invoke(loader.getController());

            Scene scene = new Scene(root);
            Stage stage = new Stage();

            stage.initModality(Modality.APPLICATION_MODAL);

            stage.setTitle(stageTitle);
            stage.setScene(scene);
            return new Tuple<>(loader, stage);
        }
        catch (IOException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("Error while loading application. Try restarting application. " +
                        "In case everything fails, please contact developer");
                alert.showAndWait();
            });
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void searchAction(ActionEvent actionEvent) throws InterruptedException {
        if (inputText.getText().isEmpty() || radiusInput.getText().isEmpty()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Missing input");
                alert.setContentText("Missing input (Coordinates or radius)");
                alert.showAndWait();
            });
            return;
        }

        int threadCount = 0;

        if (mastSearch) {
            executorCompletionService.submit(mastTask, null);
            ++threadCount;
        }

        if (vizierSearch) {
            executorCompletionService.submit(vizierTask, null);
            ++threadCount;
        }

        for (int i = 0; i < threadCount; ++i) {
            executorCompletionService.take();
        }

        initFxml("/ResultWindow.fxml", "Results").getSecond().show();
    }

    Runnable vizierTask = () -> {
        var vizierService = new VizierService();

        VizierCataloguesController vizierCataloguesController = vizierLoader.getController();
        var catalogues = vizierCataloguesController.getSelectedCatalogues();

        var requestURI = vizierService.createDataRequest(catalogues, inputText.getText(), radiusInput.getText(), radiusBox.getValue());
        vizierService.sendRequest(requestURI.get(0));
    };

    Runnable mastTask = () -> {
        var mastService = new MastService();
        MastMissionController mastMissionController = mastLoader.getController();
        var catalogue = new Catalogue();
        catalogue.setTables(mastMissionController.getSelectedMissions());
        var catalogues = new ArrayList<Catalogue>();
        catalogues.add(catalogue);
        var requests = mastService.createDataRequest(catalogues, inputText.getText(), radiusInput.getText(), radiusBox.getValue());

        for (var request : requests) {
            executor.execute(new MastRequest(request));
        }
    };
}
