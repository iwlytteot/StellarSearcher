package view.event;

import javafx.stage.Stage;
import org.springframework.context.ApplicationEvent;

public class VizierWindowEvent extends ApplicationEvent {
    public VizierWindowEvent(Stage stage) {
        super(stage);
    }

    public Stage getStage() {
        return ((Stage) getSource());
    }
}
