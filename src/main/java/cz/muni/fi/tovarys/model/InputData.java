package cz.muni.fi.tovarys.model;

import lombok.Data;

import java.util.List;

/**
 * Representation of a singleton from JSON object used for importing data.
 */
@Data
public class InputData {
    private List<String> vizier;
    private List<String> mast;
    private boolean simbad;
    private List<String> input;
    private String radius;
    private Radius unit;
}
