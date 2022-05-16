package cz.muni.fi.tovarys.view.event;

import javafx.stage.Stage;
import org.springframework.context.ApplicationEvent;

public class MastWindowEvent extends ApplicationEvent {
    public MastWindowEvent(Stage stage) {
        super(stage);
    }

    public Stage getStage() {
        return ((Stage) getSource());
    }
}
