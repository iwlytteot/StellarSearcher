package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class ConstraintWindowController {
    @FXML
    public Label textLabel;
    @FXML
    public TextField input;
    @FXML
    public Button confirmButton;

    private String constraint;

    public void init(String name) {
        textLabel.setText("Constraint: " + name);
    }

    public void confirmAction(ActionEvent actionEvent) {
        constraint = input.getText();
        var stage = (Stage) confirmButton.getScene().getWindow();
        stage.hide();
    }

    public void keyPressedAction(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            constraint = input.getText();
            var stage = (Stage) confirmButton.getScene().getWindow();
            stage.hide();
        }
    }

    public String getConstraint() {
        return constraint;
    }
}
