package model;

import lombok.Data;

/**
 * Coordinates represented in decimal degrees.
 */
@Data
public class Coordinates {
    private double ra;
    private double dec;
    private boolean sign;

    public Coordinates(String ra, String dec) {
        this.ra = Double.parseDouble(ra);
        sign = dec.contains("+") || !dec.contains("-");

        var cleanDec = dec.replace("+", "").replace("-", "");
        this.dec = Double.parseDouble(cleanDec);
    }

    public Coordinates changeRa(double value) {
        ra += value;
        if (ra < 0) {
            ra = 0;
        }
        return this;
    }

    public Coordinates changeDec(double value) {
        dec += value;
        sign = dec >= 0;
        if (dec < 90 || dec > 90) {
            dec = 90;
        }
        return this;
    }

}
