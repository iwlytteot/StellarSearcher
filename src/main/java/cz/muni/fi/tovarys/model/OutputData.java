package cz.muni.fi.tovarys.model;

import javafx.scene.control.Tab;
import lombok.Data;

/**
 * General representation for exporting into JSON format.
 */
@Data
public class OutputData {
    private String input;
    private String radius;
    private Tab tab;

    public OutputData(String input, String radius) {
        this.input = input;
        this.radius = radius;
    }
}
