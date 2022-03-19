package view;

import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//"This software uses source code created at the Centre de Données astronomiques de Strasbourg, France."//

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        Application.launch(StellarSearcher.class, args);
    }
}
