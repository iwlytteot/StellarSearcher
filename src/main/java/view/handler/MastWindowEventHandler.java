package view.handler;

import controller.fxml.MastMissionController;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Data;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import view.event.MastWindowEvent;

@Component
@ComponentScan(basePackages = "controller")
@Data
public class MastWindowEventHandler implements ApplicationListener<MastWindowEvent> {
    private final FxWeaver fxWeaver;
    private Stage stage;

    @Override
    public void onApplicationEvent(MastWindowEvent mastWindowEvent) {
        Stage stage = mastWindowEvent.getStage();
        stage.setScene(new Scene(fxWeaver.loadView(MastMissionController.class)));
        stage.setTitle("MAST window");
        this.stage = stage;
        stage.initModality(Modality.APPLICATION_MODAL);
    }
}
