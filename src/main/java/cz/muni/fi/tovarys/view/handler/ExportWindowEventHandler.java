package cz.muni.fi.tovarys.view.handler;

import cz.muni.fi.tovarys.controller.fxml.ExportWindowController;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Data;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import cz.muni.fi.tovarys.view.event.ExportWindowEvent;

@Component
@ComponentScan(basePackages = "controller")
@Data
public class ExportWindowEventHandler implements ApplicationListener<ExportWindowEvent> {
    private final FxWeaver fxWeaver;
    private Stage stage;

    @Override
    public void onApplicationEvent(ExportWindowEvent exportWindowEvent) {
        Stage stage = exportWindowEvent.getStage();
        stage.setScene(new Scene(fxWeaver.loadView(ExportWindowController.class)));
        stage.setTitle("Export window");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        this.stage = stage;
    }
}
