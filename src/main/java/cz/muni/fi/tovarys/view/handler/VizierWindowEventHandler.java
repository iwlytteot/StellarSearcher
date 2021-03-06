package cz.muni.fi.tovarys.view.handler;

import cz.muni.fi.tovarys.controller.fxml.VizierCataloguesController;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Data;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import cz.muni.fi.tovarys.view.event.VizierWindowEvent;

@Component
@ComponentScan(basePackages = "controller")
@Data
public class VizierWindowEventHandler implements ApplicationListener<VizierWindowEvent> {
    private final FxWeaver fxWeaver;
    private Stage stage;

    @Override
    public void onApplicationEvent(VizierWindowEvent vizierWindowEvent) {
        Stage stage = vizierWindowEvent.getStage();
        stage.setScene(new Scene(fxWeaver.loadView(VizierCataloguesController.class)));
        stage.setTitle("VizieR window");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        this.stage = stage;
    }
}
