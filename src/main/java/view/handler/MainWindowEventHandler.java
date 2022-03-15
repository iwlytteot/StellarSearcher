package view.handler;

import controller.MainWindowController;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import view.event.MainWindowEvent;

@Component
@ComponentScan(basePackages = "controller")
public class MainWindowEventHandler implements ApplicationListener<MainWindowEvent> {
    private final FxWeaver fxWeaver;

    public MainWindowEventHandler(FxWeaver fxWeaver) {
        this.fxWeaver = fxWeaver;
    }

    @Override
    public void onApplicationEvent(MainWindowEvent mainWindowEvent) {
        Stage stage = mainWindowEvent.getStage();
        stage.setScene(new Scene(fxWeaver.loadView(MainWindowController.class)));
        stage.setTitle("Main window");
        stage.show();
    }
}
