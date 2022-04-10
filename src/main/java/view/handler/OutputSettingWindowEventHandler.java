package view.handler;

import controller.fxml.OutputSettingController;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Data;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import view.event.OutputSettingWindowEvent;

@Component
@ComponentScan(basePackages = "controller")
@Data
public class OutputSettingWindowEventHandler implements ApplicationListener<OutputSettingWindowEvent> {
    private final FxWeaver fxWeaver;
    private Stage stage;

    @Override
    public void onApplicationEvent(OutputSettingWindowEvent outputSettingWindowEvent) {
        Stage stage = outputSettingWindowEvent.getStage();
        stage.setScene(new Scene(fxWeaver.loadView(OutputSettingController.class)));
        stage.setTitle("Output settings");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        this.stage = stage;
    }
}
