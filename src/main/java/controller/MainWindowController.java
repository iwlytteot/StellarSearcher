package controller;

import javafx.event.ActionEvent;
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
    public ComboBox<Radius> radiusBox;
    @FXML
    public Button mastTableButton;
    @FXML
    public Button vizierTableButton;

    private boolean vizierSearch = false, simbadSearch = false, mastSearch = false;

    public void init() {
        radiusBox.getItems().setAll(Radius.values());
        radiusBox.getSelectionModel().selectFirst();

        vizierTableButton.setDisable(true);
        mastTableButton.setDisable(true);
    }

    public void enterVizier(MouseEvent mouseEvent) {
        vizierSearch = !vizierSearch;
        rectLeft.setFill(vizierSearch ? Color.GREEN : Color.RED);
        vizierTableButton.setDisable(!vizierSearch);
    }

    public void enterSimbad(MouseEvent mouseEvent) {
        simbadSearch = !simbadSearch;
        rectMid.setFill(simbadSearch ? Color.GREEN : Color.RED);
    }

    public void enterMast(MouseEvent mouseEvent) {
        mastSearch = !mastSearch;
        rectRight.setFill(mastSearch ? Color.GREEN : Color.RED);
        mastTableButton.setDisable(!mastSearch);
    }

    public void mastTableButtonAction(ActionEvent actionEvent) {

    }

    public void vizierTableButtonAction(ActionEvent actionEvent) {
    }
}
