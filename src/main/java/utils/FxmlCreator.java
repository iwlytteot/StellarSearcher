package utils;

import controller.FilterWindowController;
import controller.MainWindowController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Table;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FxmlCreator {
    public static Stage createFilter(String stageTitle, Table table) {
        try {
            FXMLLoader loader = new FXMLLoader(FxmlCreator.class.getResource("/FilterWindow.fxml"));
            Parent root = loader.load();

            FilterWindowController filterWindowController = loader.getController();
            filterWindowController.init(table);

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setUserData(filterWindowController);

            stage.setScene(scene);
            stage.setTitle(stageTitle);
            return stage;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static Tuple<FXMLLoader, Stage> initFxml(String resourcePath, String stageTitle, boolean init) {
        try {
            FXMLLoader loader = new FXMLLoader(MainWindowController.class.getResource(resourcePath));
            Parent root = loader.load();

            if (init) {
                Method initMethod = Class.forName(loader.getController().getClass().getName()).getMethod("init");
                initMethod.invoke(loader.getController());
            }

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
}
