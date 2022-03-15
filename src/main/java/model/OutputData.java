package model;

import javafx.scene.control.Tab;
import org.springframework.stereotype.Component;

@Component
public class OutputData {
    private String input;
    private String radius;
    private String radiusType;
    private Tab tab;

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getRadius() {
        return radius;
    }

    public void setRadius(String radius) {
        this.radius = radius;
    }

    public String getRadiusType() {
        return radiusType;
    }

    public void setRadiusType(String radiusType) {
        this.radiusType = radiusType;
    }

    public Tab getTab() {
        return tab;
    }

    public void setTab(Tab tab) {
        this.tab = tab;
    }
}
