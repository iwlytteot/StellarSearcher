package model;

import lombok.Data;

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
