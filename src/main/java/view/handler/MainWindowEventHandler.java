package view.handler;

import controller.fxml.MainWindowController;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Data;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import view.event.MainWindowEvent;

@Component
@ComponentScan(basePackages = "controller")
@Data
public class MainWindowEventHandler implements ApplicationListener<MainWindowEvent> {
    private final FxWeaver fxWeaver;

    @Override
    public void onApplicationEvent(MainWindowEvent mainWindowEvent) {
        Stage stage = mainWindowEvent.getStage();
        stage.setScene(new Scene(fxWeaver.loadView(MainWindowController.class)));
        stage.setTitle("Main window");
        stage.show();
    }
}
