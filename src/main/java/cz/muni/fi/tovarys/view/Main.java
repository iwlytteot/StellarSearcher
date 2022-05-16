package cz.muni.fi.tovarys.view;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

//"This software uses source code created at the Centre de Données astronomiques de Strasbourg, France."//

@SpringBootApplication
@EnableAsync
public class Main {
    public static void main(String[] args) {
        Application.launch(StellarSearcher.class, args);
    }
}
