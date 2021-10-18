package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ConstraintWindowController {
    @FXML
    public Label textLabel;
    @FXML
    public TextField input;
    @FXML
    public Button confirmButton;

    private String name;
    private String constraint;

    public void init(String name) {
        this.name = name;
        textLabel.setText("Constraint: " + this.name);
    }

    public void confirmAction(ActionEvent actionEvent) {
        constraint = input.getText();
        var stage = (Stage) confirmButton.getScene().getWindow();
        stage.hide();
    }
}
