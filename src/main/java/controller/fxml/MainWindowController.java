package controller.fxml;

import controller.http.Request;
import controller.task.ImportController;
import controller.http.SesameResolver;
import controller.task.Searcher;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import model.*;
import model.exception.CatalogueQueryException;
import model.exception.RecursionDepthException;
import model.exception.ResolverQueryException;
import model.mirror.SimbadServer;
import model.mirror.VizierServer;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import controller.task.GridSearch;
import controller.task.MastSearch;
import view.event.MastWindowEvent;
import view.event.OutputSettingWindowEvent;
import view.event.ResultWindowEvent;
import view.event.VizierWindowEvent;
import view.handler.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Class controller for "MainWindow.fxml".
 */
@Component
@ComponentScan("model, utils")
@FxmlView("/fxml/MainWindow.fxml")
@Data
@Slf4j
public class MainWindowController {
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
    @FXML
    public Ellipse vizierEllipse;
    @FXML
    public Ellipse simbadEllipse;
    @FXML
    public Ellipse mastEllipse;

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

    private final GridSearch gridSearcher;
    private final Searcher searcher;
    private final Request vizierService;
    private final Request simbadService;
    private final MastSearch mastSearcher;
    private final ImportController importController;
    private final SesameResolver sesameResolver;

    private boolean vizierSearch = false, simbadSearch = false, mastSearch = false;
    private File importFile;

    @FXML
    public void initialize() {
        radiusBox.getItems().setAll(Radius.values());
        radiusBox.getSelectionModel().select(Radius.ARCMIN);

        vizierButton.setShape(new Ellipse(55, 45));
        simbadButton.setShape(new Ellipse(55, 45));
        mastButton.setShape(new Ellipse(55, 45));

        vizierTableButton.setDisable(true);
        mastTableButton.setDisable(true);
        searchButton.setDisable(true);
    }

    @FXML
    public void enterVizier() {
        vizierSearch = !vizierSearch;
        vizierEllipse.setFill(vizierSearch ? Color.GREEN : Color.RED);
        vizierTableButton.setDisable(!vizierSearch);
        searchButtonCheck();
    }

    @FXML
    public void enterSimbad() {
        simbadSearch = !simbadSearch;
        simbadEllipse.setFill(simbadSearch ? Color.GREEN : Color.RED);
        searchButtonCheck();
    }

    @FXML
    public void enterMast() {
        mastSearch = !mastSearch;
        mastEllipse.setFill(mastSearch ? Color.GREEN : Color.RED);
        mastTableButton.setDisable(!mastSearch);
        searchButtonCheck();
    }

    @FXML
    public void mastTableButtonAction() {
        if (mastWindowEventHandler.getStage() == null) {
            context.publishEvent(new MastWindowEvent(new Stage()));
        }
        mastWindowEventHandler.getStage().show();
    }

    @FXML
    public void vizierTableButtonAction() {
        if (vizierWindowEventHandler.getStage() == null) {
            context.publishEvent(new VizierWindowEvent(new Stage()));
        }
        vizierCataloguesController.setVizierServer(getVizierServer());
        vizierWindowEventHandler.getStage().show();
    }

    /**
     * Method that handles uppermost interaction with user when importing file.
     */
    @FXML
    public void importData() {
        FileChooser fileChooser = new FileChooser();
        importFile = fileChooser.showOpenDialog(searchButton.getScene().getWindow());
        if (importFile == null) {
            return;
        }

        if (importService.getState() != Worker.State.READY) {
            importService.cancel();
            importService.reset();
        }
        importService.start();
    }

    @FXML
    public void openOutputSetting() {
        if (outputSettingWindowEventHandler.getStage() == null) {
            context.publishEvent(new OutputSettingWindowEvent(new Stage()));
        }
        outputSettingWindowEventHandler.getStage().show();
    }

    /**
     * Method that check the input and starts the searchService.
     */
    @FXML
    public void searchAction() {
        if (inputText.getText().isEmpty() || radiusInput.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Missing input");
            alert.setContentText("Missing input");
            alert.showAndWait();
            return;
        }

        if (vizierSearch && getVizierCatalogues().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Missing catalogues");
            alert.setContentText("No catalogues from VizieR were selected");
            alert.showAndWait();
            return;
        }
        if (mastSearch && getMastMissions().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Missing catalogues");
            alert.setContentText("No missions from MAST were selected");
            alert.showAndWait();
            return;
        }

        try {
            var radius= Float.parseFloat(radiusInput.getText());
            if (radius <= 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Wrong radius");
                alert.setContentText("Radius must be non negative");
                alert.showAndWait();
                return;
            }
        } catch(NumberFormatException ex) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Wrong radius");
            alert.setContentText("Radius is not in correct format. You can use dot (.) eg: 1.2");
            alert.showAndWait();
            log.error("Wrong radius input: " + radiusInput.getText());
            return;
        }

        if (searchService.getState() != Worker.State.READY) {
            searchService.cancel();
            searchService.reset();
        }
        searchService.start();
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

    public void exit() {
        var searchServiceState = searchService.getState();
        if (searchServiceState == Worker.State.RUNNING || searchServiceState == Worker.State.SCHEDULED) {
            searchService.cancel();
        }

        var importServiceState = searchService.getState();
        if (importServiceState == Worker.State.RUNNING || importServiceState == Worker.State.SCHEDULED) {
            importService.cancel();
        }

        var exportServiceState = exportWindowController.getExportService().getState();
        if (exportServiceState == Worker.State.RUNNING || exportServiceState == Worker.State.SCHEDULED) {
            exportWindowController.getExportService().cancel();
        }
    }

    private void searchButtonCheck() {
        searchButton.setDisable(!mastSearch && !simbadSearch && !vizierSearch);
    }

    private List<Catalogue> getVizierCatalogues() {
        return vizierCataloguesController.getSelectedCatalogues();
    }

    private List<Table> getMastMissions() {
        return mastMissionController.getSelectedMissions();
    }

    private Coordinates getResolvedInput(String input) throws ExecutionException, InterruptedException {
        return sesameResolver.start(input).get();
    }

    private UserInput getUserInput() {
        return new UserInput(inputText.getText(), radiusInput.getText(), radiusBox.getValue());
    }

    /**
     * JavaFX Service, where search parameters are retrieved and where search action takes place.
     */
    private final Service<List<String>> searchService = new Service<>() {
        @Override
        protected Task<List<String>> createTask() {
            return new Task<>() {
                @Override
                protected List<String> call() throws InterruptedException, ExecutionException {
                    List<CompletableFuture<List<String>>> futures = new ArrayList<>();

                    Platform.runLater(() -> {
                        searchButton.getScene().setCursor(Cursor.WAIT);
                        infoLabel.setText("Resolving input..");
                    });
                    Coordinates resolvedInput = null;

                    //Resolving user input into coordinates into decimal degree notation
                    try {
                        resolvedInput = getResolvedInput(inputText.getText());
                    } catch (ExecutionException | InterruptedException ex) {
                        if (ex.getCause() instanceof ResolverQueryException && simbadSearch) {
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.WARNING);
                                alert.setTitle("Input");
                                alert.setContentText("Input \"" + inputText.getText() + "\" could not be resolved.");
                                alert.showAndWait();
                            });
                        }
                        searchService.cancel();
                    }

                    Platform.runLater(() -> infoLabel.setText("Downloading data.."));

                    //If VizieR button was activated
                    if (vizierSearch) {
                        futures.add(searcher.start(vizierService, getVizierCatalogues(), inputText.getText(),
                                radiusInput.getText(), radiusBox.getValue(), getVizierServer()));
                    }

                    //If SIMBAD button was activated
                    if (simbadSearch && resolvedInput != null) {
                        String coordInput = resolvedInput.getRa() + " " + resolvedInput.getDec();
                        futures.add(searcher.start(simbadService, null, coordInput,
                                radiusInput.getText(), radiusBox.getValue(), getSimbadServer()));
                    }

                    //If MAST button was activated
                    if (mastSearch) {
                        try {
                            futures.add(mastSearcher.start(getMastMissions(), inputText.getText(), radiusInput.getText(),
                                    radiusBox.getValue(), resolvedInput));
                        } catch (RecursionDepthException e) {
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.WARNING);
                                alert.setTitle("Can't get results");
                                alert.setContentText("MAST could not be queried anymore, because maximum recursion depth" +
                                        " happened. Try smaller radius or contact MAST.");
                                alert.showAndWait();
                            });
                        } catch (CatalogueQueryException e) {
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.WARNING);
                                alert.setTitle("Can't get results");
                                alert.setContentText("Search failed");
                                alert.showAndWait();
                            });
                        }
                    }

                    //Check if there is any query to be done
                    if (futures.isEmpty()) {
                        searchService.cancel();
                    }

                    //Retrieving results
                    List<List<String>> output = new ArrayList<>();
                    for (var response : futures) {
                        output.add(response.get());
                    }
                    return output.stream().flatMap(List::stream).collect(Collectors.toList());
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
            var resultList = searchService.getValue();
            var result = new HashMap<UserInput, List<String>>();
            result.put(getUserInput(), resultList);

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


    private final Service<HashMap<UserInput, List<String>>> importService = new Service<>() {
        @Override
        protected Task<HashMap<UserInput, List<String>>> createTask() {
            return new Task<>() {
                @Override
                protected HashMap<UserInput, List<String>> call() {
                    HashMap<UserInput, List<String>> output = new HashMap<>();
                    Platform.runLater(() -> {
                        searchButton.getScene().setCursor(Cursor.WAIT);
                        infoLabel.setText("Processing input file and downloading data..");
                    });
                    try {
                        output = importController.start(importFile.getAbsolutePath(),
                                getVizierServer(), getSimbadServer()).get();
                    } catch (InterruptedException | ExecutionException | CatalogueQueryException e) {
                        log.error("Error while retrieving data from import: " + e.getMessage());
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Import failed");
                            alert.setContentText("Error while importing data: " + e.getMessage());
                            alert.showAndWait();
                        });
                        importService.cancel();
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

        @Override
        protected void cancelled() {
            infoLabel.setText("");
            searchButton.getScene().setCursor(Cursor.DEFAULT);
            log.error("Import search was cancelled.");
        }

        @Override
        protected void failed() {
            infoLabel.setText("");
            searchButton.getScene().setCursor(Cursor.DEFAULT);
            log.error("Import search has failed.");
        }
    };
}
