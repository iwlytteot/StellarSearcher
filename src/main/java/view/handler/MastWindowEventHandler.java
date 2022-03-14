package view.handler;

import controller.MastMissionController;
import controller.VizierCataloguesController;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import view.event.MastWindowEvent;
import view.event.VizierWindowEvent;

@Component
@ComponentScan(basePackages = "controller")
public class MastWindowEventHandler implements ApplicationListener<MastWindowEvent> {
    private final FxWeaver fxWeaver;
    private Stage stage;

    public MastWindowEventHandler(FxWeaver fxWeaver) {
        this.fxWeaver = fxWeaver;
    }

    @Override
    public void onApplicationEvent(MastWindowEvent mastWindowEvent) {
        Stage stage = mastWindowEvent.getStage();
        stage.setScene(new Scene(fxWeaver.loadView(MastMissionController.class)));
        stage.setTitle("MAST window");
        this.stage = stage;
        stage.show();
    }

    public Stage getStage() {
        return stage;
    }
}
