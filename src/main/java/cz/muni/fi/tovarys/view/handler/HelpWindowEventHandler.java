package cz.muni.fi.tovarys.view.handler;

import cz.muni.fi.tovarys.controller.fxml.HelpWindowController;
import cz.muni.fi.tovarys.view.event.HelpWindowEvent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Data;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@Component
@ComponentScan(basePackages = "controller")
@Data
public class HelpWindowEventHandler implements ApplicationListener<HelpWindowEvent> {
    private final FxWeaver fxWeaver;
    private Stage stage;

    @Override
    public void onApplicationEvent(HelpWindowEvent helpWindowEvent) {
        Stage stage = helpWindowEvent.getStage();
        stage.setScene(new Scene(fxWeaver.loadView(HelpWindowController.class)));
        stage.setTitle("How to search");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        this.stage = stage;
    }
}
