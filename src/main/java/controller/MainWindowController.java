package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.Radius;

public class MainWindowController {
    @FXML
    public Rectangle rectLeft;
    @FXML
    public Rectangle rectMid;
    @FXML
    public Rectangle rectRight;
    @FXML
    public Button vizierButton;
    @FXML
    public Button simbadButton;
    @FXML
    public Button mastButton;
    @FXML
    public ComboBox radiusBox;

    public void init() {
        radiusBox.getItems().setAll(Radius.values());
        radiusBox.getSelectionModel().selectFirst();
    }

    public void enterVizier(MouseEvent mouseEvent) {
        rectLeft.setFill(rectLeft.getFill() == Color.RED ? Color.GREEN : Color.RED);
    }

    public void enterSimbad(MouseEvent mouseEvent) {
        rectMid.setFill(rectMid.getFill() == Color.RED ? Color.GREEN : Color.RED);
    }

    public void enterMast(MouseEvent mouseEvent) {
        rectRight.setFill(rectRight.getFill() == Color.RED ? Color.GREEN : Color.RED);
    }
}
