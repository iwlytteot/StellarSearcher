package cz.muni.fi.tovarys.model;

/**
 * Abstract representation of object that holds name and information.
 */
@lombok.Data
public abstract class Data {
    private String name;
    private String info;
}
