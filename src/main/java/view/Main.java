package view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//"This software uses source code created at the Centre de Données astronomiques de Strasbourg, France."//

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("starting ...");
        StellarSearcher.main(args);
    }
}
