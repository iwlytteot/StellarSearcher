package cz.muni.fi.tovarys.model;

import lombok.Data;

/**
 * Coordinates represented in decimal degrees.
 */
@Data
public class Coordinates {
    private double ra;
    private double dec;

    public Coordinates(String ra, String dec) {
        this.ra = Double.parseDouble(ra);
        this.dec = Double.parseDouble(dec);
    }

    public Coordinates(double ra, double dec) {
        offsetRa(ra);
        offsetDec(dec);
    }

    public Coordinates(Coordinates c) {
        this.ra = c.getRa();
        this.dec = c.getDec();
    }

    public void offsetRa(double value) {
        ra += value;
        if (ra < 0) {
            ra = 0;
        }
        if (ra > 360) {
            ra = 360;
        }
    }

    public void offsetDec(double value) {
        dec += value;
        if (dec < -90) {
            dec = -90;
        }
        if (dec > 90) {
            dec = 90;
        }
    }

    public void offsetRaDec(double value) {
        offsetRa(value);
        offsetDec(value);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Coordinates)) return false;

        Coordinates coordinates = (Coordinates) o;

        return ra == coordinates.getRa() && dec == coordinates.getDec();
    }

    @Override
    public String toString() {
        return ra + " " + dec;
    }

}
