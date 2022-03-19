package model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import view.Main;

public class CatalogueQueryException extends RuntimeException {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public CatalogueQueryException() {}
}
