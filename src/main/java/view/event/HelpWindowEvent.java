package view.event;

import javafx.stage.Stage;
import org.springframework.context.ApplicationEvent;

public class HelpWindowEvent extends ApplicationEvent {
    public HelpWindowEvent(Stage stage) {
        super(stage);
    }

    public Stage getStage() {
        return ((Stage) getSource());
    }
}
