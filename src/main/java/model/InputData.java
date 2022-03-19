package model;

import lombok.Data;

import java.util.List;

@Data
public class InputData {
    private List<String> vizier;
    private List<String> mast;
    private boolean simbad;
    private List<String> input;
    private String radius;
    private Radius unit;
}
