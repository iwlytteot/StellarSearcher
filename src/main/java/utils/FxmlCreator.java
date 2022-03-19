package utils;

import controller.ConstraintWindowController;
import controller.FilterWindowController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Table;

import java.io.IOException;

/**
 * Class for creating JavaFX windows from FXML files.
 */
public class FxmlCreator {
    public static Stage createFilterWindow(String stageTitle, Table table) {
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

    public static Stage createConstraintWindow(String constraintName) {
        try {
            FXMLLoader loader = new FXMLLoader(FxmlCreator.class.getResource("/ConstraintWindow.fxml"));
            Parent root = loader.load();

            ConstraintWindowController constraintWindowController = loader.getController();
            constraintWindowController.init(constraintName);

            Scene scene = new Scene(root);
            Stage stage = new Stage();

            stage.setUserData(constraintWindowController);
            stage.setScene(scene);
            stage.setTitle("Constraint window");
            return stage;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
