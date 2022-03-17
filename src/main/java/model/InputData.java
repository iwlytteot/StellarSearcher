package model;

import java.util.List;

public class InputData {
    private List<String> vizier;
    private List<String> mast;
    private boolean simbad;
    private List<String> input;
    private String radius;
    private Radius unit;

    public void setVizier(List<String> vizier) {
        this.vizier = vizier;
    }

    public void setMast(List<String> mast) {
        this.mast = mast;
    }

    public void setSimbad(boolean simbad) {
        this.simbad = simbad;
    }

    public void setInput(List<String> input) {
        this.input = input;
    }

    public void setRadius(String radius) {
        this.radius = radius;
    }

    public void setUnit(Radius unit) {
        this.unit = unit;
    }

    public List<String> getVizier() {
        return vizier;
    }

    public List<String> getMast() {
        return mast;
    }

    public boolean isSimbad() {
        return simbad;
    }

    public List<String> getInput() {
        return input;
    }

    public String getRadius() {
        return radius;
    }

    public Radius getUnit() {
        return unit;
    }
}
