package view.event;

import javafx.stage.Stage;
import org.springframework.context.ApplicationEvent;

public class MainWindowEvent extends ApplicationEvent {
    public MainWindowEvent(Stage stage) {
        super(stage);
    }

    public Stage getStage() {
        return ((Stage) getSource());
    }
}
