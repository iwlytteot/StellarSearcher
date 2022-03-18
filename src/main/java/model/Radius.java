package model;

public enum Radius {
    DEG("deg"),
    ARCSEC("arcsec"),
    ARCMIN("arcmin");

    public final String name;

    Radius(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
