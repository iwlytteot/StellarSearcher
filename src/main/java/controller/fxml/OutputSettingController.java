package controller.fxml;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Data;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;

/**
 * Class controller for "OutputSettingWindow.fxml"
 */
@Controller
@FxmlView("/OutputSettingWindow.fxml")
@Data
public class OutputSettingController {
    @FXML
    public TextField inputCols;
    @FXML
    public TextField inputRows;

    private int numOfCols = 10;
    private int numOfRows = -1;

    public void save() {
        if (!inputRows.getText().isEmpty()) {
            try {
                numOfRows = Integer.parseInt(inputRows.getText());
                if (numOfRows <= 0) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Wrong rows");
                    alert.setContentText("Number of rows must be non negative");
                    alert.showAndWait();
                    inputRows.setText("");
                    numOfRows = -1;
                    return;
                }
            } catch(NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Wrong rows");
                alert.setContentText("Number of rows is not an integer");
                alert.showAndWait();
                inputRows.setText("");
                numOfRows = -1;
                return;
            }
        }
        else {
            numOfRows = -1;
        }

        if (!inputCols.getText().isEmpty()) {
            try {
                numOfCols = Integer.parseInt(inputCols.getText());
                if (numOfCols <= 0) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Wrong columns");
                    alert.setContentText("Number of columns must be non negative");
                    alert.showAndWait();
                    inputCols.setText("10");
                    numOfCols = 10;
                    return;
                }
            } catch(NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Wrong columns");
                alert.setContentText("Number of columns is not an integer");
                alert.showAndWait();
                inputCols.setText("10");
                numOfCols = 10;
                return;
            }
        }
        else {
            numOfCols = -1;
        }

        var stage = (Stage) inputCols.getScene().getWindow();
        stage.hide();
    }
}
