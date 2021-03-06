package cz.muni.fi.tovarys.controller.fxml;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.stage.DirectoryChooser;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import cz.muni.fi.tovarys.model.OutputData;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import cz.muni.fi.tovarys.utils.DataExporter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Class controller for "ExportWindow.fxml"
 */
@Component
@FxmlView("/fxml/ExportWindow.fxml")
@Data
@Slf4j
public class ExportWindowController {
    @FXML
    public Label directoryLabel;

    private final ResultWindowController resultWindowController;
    private File selectedDirectory;

    /**
     * Opens directory chooser and then sets the directory and displays the path.
     */
    @FXML
    public void directoryDialog() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        selectedDirectory = directoryChooser.showDialog(directoryLabel.getScene().getWindow());
        directoryLabel.setText(selectedDirectory != null ? selectedDirectory.getAbsolutePath() : "");
    }

    /**
     * If no directory was chosen, displays error, else sets 'proceed' to true (for further processing)
     * and closes the window
     */
    @FXML
    public void proceedAction() {
        if (selectedDirectory == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setContentText("You must choose directory");
            alert.showAndWait();
            return;
        }

        directoryLabel.getScene().setCursor(Cursor.WAIT);

        if (exportService.getState() != Worker.State.READY) {
            exportService.cancel();
            exportService.reset();
        }
        exportService.start();
    }

    private final Service<Void> exportService = new Service<>() {
        @Override
        protected Task<Void> createTask() {
            return new Task<>() {
                @Override
                protected Void call() {
                    ObjectMapper mapper = new ObjectMapper();
                    SimpleModule module = new SimpleModule("DataSerial", new Version(1, 0, 0, null, null, null));
                    module.addSerializer(OutputData.class, new DataExporter());
                    mapper.registerModule(module);

                    //Mapping Tab object into JSON object via ObjectMapper that uses DataExporter as its map.
                    int i = 0;
                    for (var inputTab : resultWindowController.getTabPane().getTabs()) {
                        var input = Arrays.stream(inputTab.getText().split(";")).collect(Collectors.toList());
                        if (Files.exists(Path.of(selectedDirectory.getAbsolutePath() + File.separator + "input-" + i))) {
                            try {
                                FileSystemUtils.deleteRecursively(Path.of(selectedDirectory.getAbsolutePath() + File.separator + "input-" + i));
                            } catch (IOException e) {
                               log.error("Error during deleting folder " + selectedDirectory.getAbsolutePath() + File.separator + "input-" + i);
                            }
                        }
                        var dir = new File(selectedDirectory.getAbsolutePath() + File.separator + "input-" + i);
                        if(!dir.mkdir()) {
                            log.error("Could not create directory " + selectedDirectory.getAbsolutePath() + File.separator + "input-" + i);
                            exportService.cancel();
                            return null;
                        }
                        for (var tab : ((TabPane) inputTab.getContent()).getTabs()) {
                            try {
                                FileWriter myWriter = new FileWriter(selectedDirectory.getAbsolutePath() + File.separator + "input-" + i +
                                        File.separator +
                                        tab.getText().replaceAll("[^a-zA-Z0-9-_.]", "_") + ".txt");
                                var outputData = new OutputData(input.get(0), input.get(1));
                                outputData.setTab(tab);
                                myWriter.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(outputData));
                                myWriter.close();
                            } catch (IOException e) {
                                log.error("Error during exporting: " + e.getMessage());
                            }
                        }
                        ++i;
                    }
                    return null;
                }
            };
        }

        @Override
        protected void succeeded() {
            directoryLabel.getScene().setCursor(Cursor.DEFAULT);

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Export");
            alert.setContentText("Export was successful");
            alert.showAndWait();
        }

        @Override
        protected void cancelled() {
            directoryLabel.getScene().setCursor(Cursor.DEFAULT);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export");
            alert.setContentText("Export has failed");
            alert.showAndWait();
        }
    };
}
