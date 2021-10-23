package utils;

import controller.FilterWindowController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Table;

import java.io.IOException;

public class FxmlCreator {
    public static Stage create(String stageTitle, Table table) {
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
