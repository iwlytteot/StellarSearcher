package utils;

import controller.fxml.ConstraintWindowController;
import controller.fxml.FilterWindowController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import model.Table;

import java.io.IOException;

/**
 * Class for creating JavaFX windows from FXML files.
 */
@Slf4j
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
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            return stage;

        } catch (IOException e) {
            log.error(e.getMessage());
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
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            return stage;

        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
