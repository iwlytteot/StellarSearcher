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
        this.dec = Double.parseDouble(dec);
        this.sign = this.dec >= 0;
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
        sign = dec >= 0;
        if (dec < -90) {
            dec = -90;
        }
        if (dec > 90) {
            dec = 90;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Coordinates)) return false;

        Coordinates coordinates = (Coordinates) o;

        return ra == coordinates.getRa() && dec == coordinates.getDec() && sign == coordinates.isSign();
    }

}
