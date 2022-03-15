package view.handler;

import controller.ResultWindowController;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import view.event.ResultWindowEvent;

@Component
@ComponentScan(basePackages = "controller")
public class ResultWindowEventHandler implements ApplicationListener<ResultWindowEvent> {
    private final FxWeaver fxWeaver;
    private Stage stage;

    public ResultWindowEventHandler(FxWeaver fxWeaver) {
        this.fxWeaver = fxWeaver;
    }

    @Override
    public void onApplicationEvent(ResultWindowEvent resultWindowEvent) {
        Stage stage = resultWindowEvent.getStage();
        stage.setScene(new Scene(fxWeaver.loadView(ResultWindowController.class)));
        stage.setTitle("Result window");
        this.stage = stage;
        stage.show();
    }

    public Stage getStage() {
        return stage;
    }
}
