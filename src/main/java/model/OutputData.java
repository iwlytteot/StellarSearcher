package model;

import javafx.scene.control.Tab;
import lombok.Data;

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
