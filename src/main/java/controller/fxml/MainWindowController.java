package controller.fxml;

import controller.ImportControllerTask;
import controller.http.GetDataTask;
import controller.http.SesameResolver;
import controller.http.mast.MastService;
import controller.http.simbad.SimbadService;
import controller.http.vizier.VizierService;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import model.*;
import model.mirror.MastServer;
import model.mirror.SimbadServer;
import model.mirror.VizierServer;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import view.event.MastWindowEvent;
import view.event.OutputSettingWindowEvent;
import view.event.ResultWindowEvent;
import view.event.VizierWindowEvent;
import view.handler.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Class controller for "MainWindow.fxml".
 */
@Component
@ComponentScan("model")
@FxmlView("/MainWindow.fxml")
@Data
@Slf4j
public class MainWindowController {
    private final ConfigurableApplicationContext context;
    private final VizierWindowEventHandler vizierWindowEventHandler;
    private final MastWindowEventHandler mastWindowEventHandler;
    private final ResultWindowEventHandler resultWindowEventHandler;
    private final OutputSettingWindowEventHandler outputSettingWindowEventHandler;
    private final ExportWindowEventHandler exportWindowEventHandler;

    private final VizierCataloguesController vizierCataloguesController;
    private final MastMissionController mastMissionController;
    private final ResultWindowController resultWindowController;
    private final OutputSettingController outputSettingController;
    private final ExportWindowController exportWindowController;

    @FXML
    public Rectangle rectLeft;
    @FXML
    public Rectangle rectMid;
    @FXML
    public Rectangle rectRight;
    @FXML
    public Button vizierButton;
    @FXML
    public Button simbadButton;
    @FXML
    public Button mastButton;
    @FXML
    public ComboBox<Radius> radiusBox;
    @FXML
    public Button mastTableButton;
    @FXML
    public Button vizierTableButton;
    @FXML
    public Button searchButton;
    @FXML
    public TextField inputText;
    @FXML
    public TextField radiusInput;
    @FXML
    public RadioMenuItem vizierFrance;
    @FXML
    public RadioMenuItem vizierJapan;
    @FXML
    public RadioMenuItem vizierIndia;
    @FXML
    public RadioMenuItem vizierUsa;
    @FXML
    public RadioMenuItem simbadFrance;
    @FXML
    public RadioMenuItem simbadUsa;
    @FXML
    public Label infoLabel;

    private boolean vizierSearch = false, simbadSearch = false, mastSearch = false;
    private final List<String> affectedTables = Collections.synchronizedList(new ArrayList<>());

    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private File importFile;

    @FXML
    public void initialize() {
        radiusBox.getItems().setAll(Radius.values());
        radiusBox.getSelectionModel().select(Radius.ARCMIN);

        vizierTableButton.setDisable(true);
        mastTableButton.setDisable(true);
        searchButton.setDisable(true);
    }

    public void exit() {
        executorService.shutdown();
        searchService.cancel();
        importService.cancel();

        var exportServiceState = exportWindowController.getExportService().getState();
        if (exportServiceState == Worker.State.RUNNING || exportServiceState == Worker.State.SCHEDULED) {
            exportWindowController.getExportService().cancel();
        }

        var catalogueServiceState = vizierCataloguesController.getCatalogueRequestService().getState();
        if (catalogueServiceState == Worker.State.RUNNING || exportServiceState == Worker.State.SCHEDULED) {
            vizierCataloguesController.getCatalogueRequestService().cancel();
        }
    }

    public void enterVizier() {
        vizierSearch = !vizierSearch;
        rectLeft.setFill(vizierSearch ? Color.GREEN : Color.RED);
        vizierTableButton.setDisable(!vizierSearch);
        SearchButtonCheck();
    }

    public void enterSimbad() {
        simbadSearch = !simbadSearch;
        rectMid.setFill(simbadSearch ? Color.GREEN : Color.RED);
        SearchButtonCheck();
    }

    public void enterMast() {
        mastSearch = !mastSearch;
        rectRight.setFill(mastSearch ? Color.GREEN : Color.RED);
        mastTableButton.setDisable(!mastSearch);
        SearchButtonCheck();
    }

    public void mastTableButtonAction() {
        if (mastWindowEventHandler.getStage() == null) {
            context.publishEvent(new MastWindowEvent(new Stage()));
        }
        mastWindowEventHandler.getStage().show();
    }

    public void vizierTableButtonAction() {
        if (vizierWindowEventHandler.getStage() == null) {
            context.publishEvent(new VizierWindowEvent(new Stage()));
        }
        vizierCataloguesController.setVizierServer(getVizierServer());
        vizierWindowEventHandler.getStage().show();
    }

    private void SearchButtonCheck() {
        searchButton.setDisable(!mastSearch && !simbadSearch && !vizierSearch);
    }

    /**
     * Method that check the input and starts the searchService.
     */
    public void searchAction() {
        if (inputText.getText().isEmpty() || radiusInput.getText().isEmpty()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Missing input");
                alert.setContentText("Missing input");
                alert.showAndWait();
            });
            return;
        }
        if (vizierSearch) {
            if (getVizierCatalogues().isEmpty()) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Missing catalogues");
                    alert.setContentText("No catalogues from VizieR was selected");
                    alert.showAndWait();
                });
                return;
            }
        }

        try {
            var radius= Float.parseFloat(radiusInput.getText());
            if (radius <= 0) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Wrong radius");
                    alert.setContentText("Radius must be non negative");
                    alert.showAndWait();
                });
                return;
            }
        } catch(NumberFormatException ex) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Wrong radius");
                alert.setContentText("Radius is not in correct format. Use dot (.) eg: 1.2");
                alert.showAndWait();
            });
            log.error("Wrong radius input: " + radiusInput.getText());
            return;
        }

        Platform.runLater(() -> searchButton.getScene().setCursor(Cursor.WAIT));

        if (searchService.getState() != Worker.State.READY) {
            searchService.cancel();
            searchService.reset();
        }
        searchService.start();
    }

    private List<Catalogue> getVizierCatalogues() {
        return vizierCataloguesController.getSelectedCatalogues();
    }

    private List<Table> getMastMissions() {
        return mastMissionController.getSelectedMissions();
    }

    private String getResolvedInput(String input) throws ExecutionException, InterruptedException {
        return executorService.submit(new SesameResolver(input)).get();
    }

    private UserInput getUserInput() {
        return new UserInput(inputText.getText(), radiusInput.getText(), radiusBox.getValue());
    }

    public String getVizierServer() {
        if (vizierFrance.isSelected()) {
            return VizierServer.CDS_FRANCE;
        }
        else if (vizierJapan.isSelected()) {
            return VizierServer.ADAC_TOKYO;
        }
        else if (vizierIndia.isSelected()) {
            return VizierServer.IUCAA_PUNE;
        }
        return VizierServer.CFA_HARVARD;
    }

    public String getSimbadServer() {
        if (simbadFrance.isSelected()) {
            return SimbadServer.CDS_FRANCE;
        }
        return SimbadServer.CFA_HARVARD;
    }

    /**
     * JavaFX Service, where search parameters are retrieved and where search action takes place.
     */
    private final Service<List<List<String>>> searchService = new Service<>() {
        @Override
        protected Task<List<List<String>>> createTask() {
            return new Task<>() {
                @Override
                protected List<List<String>> call() throws ExecutionException, InterruptedException {
                    List<Callable<List<String>>> tasks = new ArrayList<>();

                    //If MAST button was activated
                    if (mastSearch) {
                        var catalogue = new Catalogue();
                        catalogue.setTables(getMastMissions());
                        var catalogues = new ArrayList<Catalogue>();
                        catalogues.add(catalogue);
                        tasks.add(new GetDataTask<>(catalogues,
                                inputText.getText(), radiusInput.getText(), radiusBox.getValue(), MastService.class,
                                MastServer.MAST_DEFAULT));
                    }

                    //If VizieR button was activated
                    if (vizierSearch) {
                        tasks.add(new GetDataTask<>(getVizierCatalogues(),
                                inputText.getText(), radiusInput.getText(), radiusBox.getValue(), VizierService.class,
                                getVizierServer()));
                    }

                    //If SIMBAD button was activated
                    if (simbadSearch) {
                        tasks.add(new GetDataTask<>(null,
                                getResolvedInput(inputText.getText()), radiusInput.getText(), radiusBox.getValue(),
                                SimbadService.class, getSimbadServer()));
                    }
                    Platform.runLater(() -> infoLabel.setText("Fetching data.."));
                    var responses = executorService.invokeAll(tasks);
                    var output = new ArrayList<List<String>>();
                    for (var response : responses) {
                        output.add(response.get());
                    }
                    return output;
                }
            };
        }

        /**
         * If searching was successful and no error occurred during retrieving data, then
         * Result Window is invoked and filled with respective data.
         */
        @Override
        protected void succeeded() {
            if (resultWindowEventHandler.getStage() == null) {
                context.publishEvent(new ResultWindowEvent(new Stage()));
            }
            var flatList = searchService.getValue().stream().flatMap(List::stream).collect(Collectors.toList());
            var result = new HashMap<UserInput, List<String>>();
            result.put(getUserInput(), flatList);

            resultWindowController.fill(result, outputSettingController.getNumOfCols(), outputSettingController.getNumOfRows());
            searchButton.getScene().setCursor(Cursor.DEFAULT);
            infoLabel.setText("");
            resultWindowEventHandler.getStage().show();
        }

        @Override
        protected void cancelled() {
            searchButton.getScene().setCursor(Cursor.DEFAULT);
            infoLabel.setText("");
            log.warn("Search action was cancelled, the input was: "
                    + inputText.getText() + ", " + radiusInput.getText() + " " + radiusBox.getValue());
        }

        @Override
        protected void failed() {
            searchButton.getScene().setCursor(Cursor.DEFAULT);
            infoLabel.setText("");
            log.error("Search action has failed, the input was: "
                    + inputText.getText() + ", " + radiusInput.getText() + " " + radiusBox.getValue());
        }
    };

    /**
     * Method that handles uppermost interaction with user when importing file.
     */
    public void importData() {
        FileChooser fileChooser = new FileChooser();
        importFile = fileChooser.showOpenDialog(searchButton.getScene().getWindow());
        if (importFile == null) {
            return;
        }
        searchButton.getScene().setCursor(Cursor.WAIT);

        if (importService.getState() != Worker.State.READY) {
            importService.cancel();
            importService.reset();
        }
        importService.start();
    }

    private final Service<HashMap<UserInput, List<String>>> importService = new Service<>() {
        @Override
        protected Task<HashMap<UserInput, List<String>>> createTask() {
            return new Task<>() {
                @Override
                protected HashMap<UserInput, List<String>> call() {
                    HashMap<UserInput, List<String>> output = new HashMap<>();
                    Platform.runLater(() -> infoLabel.setText("Fetching data.."));
                    try {
                        output = executorService.submit(new ImportControllerTask(importFile.getAbsolutePath(),
                                getVizierServer(), getSimbadServer())).get();
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("Error while retrieving data from import: " + e.getMessage());
                    }
                    return output;
                }
            };
        }

        /**
         * If searching was successful and no error occurred during retrieving data, then
         * Result Window is invoked and filled with respective data.
         */
        @Override
        protected void succeeded() {
            if (resultWindowEventHandler.getStage() == null) {
                context.publishEvent(new ResultWindowEvent(new Stage()));
            }
            resultWindowController.fill(importService.getValue(),
                    outputSettingController.getNumOfCols(), outputSettingController.getNumOfRows());
            infoLabel.setText("");
            searchButton.getScene().setCursor(Cursor.DEFAULT);
            resultWindowEventHandler.getStage().show();
        }
    };

    public void openOutputSetting() {
        if (outputSettingWindowEventHandler.getStage() == null) {
            context.publishEvent(new OutputSettingWindowEvent(new Stage()));
        }
        outputSettingWindowEventHandler.getStage().show();
    }
}
