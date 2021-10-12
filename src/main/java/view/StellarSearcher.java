package view;

import controller.MainWindowController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Catalogue;

public class StellarSearcher extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
        Parent root = loader.load();

        MainWindowController mainWindowController = loader.getController();
        mainWindowController.init();

        stage.setTitle("Main window");

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

        var result = Catalogue.parseMetaData("catalogue-metadata.tsv");
        System.out.println(result.size());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
