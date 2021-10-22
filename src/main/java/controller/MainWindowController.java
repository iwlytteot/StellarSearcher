package controller;

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
import model.Radius;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

    public void init() {
        vizierStage = initFxml("/VizierCataloguesWindow.fxml", "Vizier catalogues");
        mastStage = initFxml("/MastMissionWindow.fxml", "MAST missions");

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

    private Stage initFxml(String resourcePath, String stageTitle) {
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
            return stage;
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
}
