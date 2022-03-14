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
}
