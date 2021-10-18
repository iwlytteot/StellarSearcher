package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ConstraintWindowController {
    @FXML
    public Label textLabel;

    private String name;
    private String constraint;

    public void init(String name) {
        this.name = name;
        textLabel.setText("Constraint: " + this.name);
    }
}
