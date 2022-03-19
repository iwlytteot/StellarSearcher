package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * Class controller for "ConstraintWindow.fxml"
 */
public class ConstraintWindowController {
    @FXML
    public Label textLabel;
    @FXML
    public TextField input;
    @FXML
    public Button confirmButton;

    private String constraint;

    /**
     * Sets the name of window
     * @param name name
     */
    public void init(String name) {
        textLabel.setText("Constraint: " + name);
    }

    /**
     * Sets constraint and closes the window
     */
    public void confirmAction() {
        constraint = input.getText();
        var stage = (Stage) confirmButton.getScene().getWindow();
        stage.hide();
    }

    /**
     * Sets constraint if enter was pressed and closes the window
     * @param keyEvent event
     */
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
