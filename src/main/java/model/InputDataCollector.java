package model;

import lombok.Data;

import java.util.List;

/**
 * Representation that holds singletons from JSON used for importing data.
 */
@Data
public class InputDataCollector {
    private List<InputData> targets;
}
