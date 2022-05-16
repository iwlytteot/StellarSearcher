package cz.muni.fi.tovarys.view.event;

import javafx.stage.Stage;
import org.springframework.context.ApplicationEvent;

public class ExportWindowEvent extends ApplicationEvent {
    public ExportWindowEvent(Stage stage) {
        super(stage);
    }

    public Stage getStage() {
        return ((Stage) getSource());
    }
}
