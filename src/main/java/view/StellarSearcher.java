package view;

import controller.MainWindowController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StellarSearcher extends Application {

    private MainWindowController mainWindowController;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
        Parent root = loader.load();

        this.mainWindowController = loader.getController();
        mainWindowController.init();

        stage.setTitle("Main window");

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        mainWindowController.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
