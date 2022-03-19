package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.Data;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Class controller for "ExportWindow.fxml"
 */
@Component
@FxmlView("/ExportWindow.fxml")
@Data
public class ExportWindowController {
    @FXML
    public Label directoryLabel;

    private boolean proceed = false;

    private File selectedDirectory;

    /**
     * Opens directory chooser and then sets the directory and displays the path.
     */
    public void directoryDialog() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        selectedDirectory = directoryChooser.showDialog(directoryLabel.getScene().getWindow());
        directoryLabel.setText(selectedDirectory != null ? selectedDirectory.getAbsolutePath() : "");
    }

    /**
     * If no directory was chosen, displays error, else sets 'proceed' to true (for further processing)
     * and closes the window
     */
    public void proceedAction() {
        if (selectedDirectory == null) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setContentText("You must choose directory");
                alert.showAndWait();
            });
            return;
        }
        proceed = true;
        var stage = (Stage) directoryLabel.getScene().getWindow();
        stage.hide();
    }
}
