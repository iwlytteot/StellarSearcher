package cz.muni.fi.tovarys.model;

import lombok.Data;

/**
 * Representation for user's input that was written into TextFields, such as input, radius and type of radius.
 */
@Data
public class UserInput {

    private final String input;
    private final String radius;
    private final Radius type;

    @Override
    public String toString() {
        return input + ";" + radius + " " + type.toString();
    }
}
