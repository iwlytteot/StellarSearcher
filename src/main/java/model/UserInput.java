package model;

public class UserInput {

    private final String input;
    private final String radius;
    private final Radius type;

    public UserInput(String input, String radius, Radius type) {
        this.input = input;
        this.radius = radius;
        this.type = type;
    }

    public String getInput() {
        return input;
    }

    public String getRadius() {
        return radius;
    }

    public Radius getType() {
        return type;
    }

    @Override
    public String toString() {
        return input + ";" + radius + " " + type.toString();
    }
}
