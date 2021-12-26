package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class ExportWindowController {
    @FXML
    public Label directoryLabel;
    @FXML
    public CheckBox mergeCheckBox;

    private boolean proceed = false;

    private File selectedDirectory;

    public void directoryDialog(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        selectedDirectory = directoryChooser.showDialog(directoryLabel.getScene().getWindow());
        directoryLabel.setText(selectedDirectory != null ? selectedDirectory.getAbsolutePath() : "");
    }

    public void proceedAction(ActionEvent actionEvent) {
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

    public CheckBox getMergeCheckBox() {
        return mergeCheckBox;
    }

    public boolean isProceed() {
        return proceed;
    }

    public File getSelectedDirectory() {
        return selectedDirectory;
    }

    public void setProceed(boolean proceed) {
        this.proceed = proceed;
    }
}
