package view.handler;

import controller.VizierCataloguesController;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import view.event.VizierWindowEvent;

@Component
@ComponentScan(basePackages = "controller")
public class VizierWindowEventHandler implements ApplicationListener<VizierWindowEvent> {
    private final FxWeaver fxWeaver;
    private Stage stage;

    public VizierWindowEventHandler(FxWeaver fxWeaver) {
        this.fxWeaver = fxWeaver;
    }

    @Override
    public void onApplicationEvent(VizierWindowEvent vizierWindowEvent) {
        Stage stage = vizierWindowEvent.getStage();
        stage.setScene(new Scene(fxWeaver.loadView(VizierCataloguesController.class)));
        stage.setTitle("VizieR window");
        this.stage = stage;
        stage.show();
    }

    public Stage getStage() {
        return stage;
    }
}
