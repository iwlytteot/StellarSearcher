package cz.muni.fi.tovarys.view.handler;

import cz.muni.fi.tovarys.controller.fxml.MainWindowController;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Data;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import cz.muni.fi.tovarys.view.event.MainWindowEvent;

@Component
@ComponentScan(basePackages = "controller")
@Data
public class MainWindowEventHandler implements ApplicationListener<MainWindowEvent> {
    private final FxWeaver fxWeaver;

    @Override
    public void onApplicationEvent(MainWindowEvent mainWindowEvent) {
        Stage stage = mainWindowEvent.getStage();
        stage.setScene(new Scene(fxWeaver.loadView(MainWindowController.class)));
        stage.setTitle("Stellar Searcher");
        stage.setResizable(false);
        stage.show();
    }
}
