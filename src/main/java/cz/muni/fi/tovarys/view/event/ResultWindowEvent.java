package cz.muni.fi.tovarys.view.event;

import javafx.stage.Stage;
import org.springframework.context.ApplicationEvent;

public class ResultWindowEvent extends ApplicationEvent {
    public ResultWindowEvent(Stage stage) {
        super(stage);
    }

    public Stage getStage() {
        return ((Stage) getSource());
    }
}
