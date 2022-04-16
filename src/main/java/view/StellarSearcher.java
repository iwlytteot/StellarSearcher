package view;

import controller.fxml.MainWindowController;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import view.event.MainWindowEvent;

@Component
public class StellarSearcher extends Application {
    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        applicationContext = new SpringApplicationBuilder(Main.class).run();
    }

    @Override
    public void start(Stage stage) {
        applicationContext.publishEvent(new MainWindowEvent(stage));
    }

    @Override
    public void stop() {
        applicationContext.getBean(MainWindowController.class).exit();
        applicationContext.close();
    }
}
