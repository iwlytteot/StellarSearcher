package cz.muni.fi.tovarys.view.handler;

import cz.muni.fi.tovarys.controller.fxml.ResultWindowController;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Data;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import cz.muni.fi.tovarys.view.event.ResultWindowEvent;

@Component
@ComponentScan(basePackages = "cz.muni.fi.tovarys.controller")
@Data
public class ResultWindowEventHandler implements ApplicationListener<ResultWindowEvent> {
    private final FxWeaver fxWeaver;
    private final ResultWindowController resultWindowController;
    private Stage stage;

    @Override
    public void onApplicationEvent(ResultWindowEvent resultWindowEvent) {
        Stage stage = resultWindowEvent.getStage();
        stage.setScene(new Scene(fxWeaver.loadView(ResultWindowController.class)));
        stage.setTitle("Result window");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.setOnCloseRequest(event -> Platform.runLater(() -> resultWindowController.getTabPane().getTabs().clear()));
        this.stage = stage;
    }
}
