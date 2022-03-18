package model;

import javafx.scene.control.Tab;

public class OutputData {
    private String input;
    private String radius;
    private Tab tab;

    public OutputData(String input, String radius) {
        this.input = input;
        this.radius = radius;
    }

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

    public Tab getTab() {
        return tab;
    }

    public void setTab(Tab tab) {
        this.tab = tab;
    }
}
