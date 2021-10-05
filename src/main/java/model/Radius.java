package model;

public enum Radius {
    DEG("deg"),
    ARCSEC("arc sec"),
    ARCMIN("arc min");

    public final String name;

    Radius(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
