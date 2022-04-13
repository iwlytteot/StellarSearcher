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
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import model.*;
import model.exception.CatalogueQueryException;
import model.exception.RecursionDepthException;
import model.exception.ResolverQueryException;
import model.exception.TimeoutQueryException;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private boolean vizierSearch = false, simbadSearch = false, mastSearch = false;
    private final List<String> affectedTables = Collections.synchronizedList(new ArrayList<>());
    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
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
        searchButton.getScene().setCursor(Cursor.WAIT);

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
        if (vizierSearch) {
            if (getVizierCatalogues().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Missing catalogues");
                alert.setContentText("No catalogues from VizieR were selected");
                alert.showAndWait();
                return;
            }
        }

        if (mastSearch) {
            if (getMastMissions().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Missing catalogues");
                alert.setContentText("No missions from MAST were selected");
                alert.showAndWait();
                return;
            }
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
            alert.setContentText("Radius is not in correct format. Use dot (.) eg: 1.2");
            alert.showAndWait();
            log.error("Wrong radius input: " + radiusInput.getText());
            return;
        }

        searchButton.getScene().setCursor(Cursor.WAIT);

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
        executorService.shutdown();
        importService.cancel();

        var importServiceState = searchService.getState();
        if (importServiceState == Worker.State.RUNNING || importServiceState == Worker.State.SCHEDULED) {
            searchService.cancel();
        }

        var exportServiceState = exportWindowController.getExportService().getState();
        if (exportServiceState == Worker.State.RUNNING || exportServiceState == Worker.State.SCHEDULED) {
            exportWindowController.getExportService().cancel();
        }

        var catalogueServiceState = vizierCataloguesController.getCatalogueRequestService().getState();
        if (catalogueServiceState == Worker.State.RUNNING || exportServiceState == Worker.State.SCHEDULED) {
            vizierCataloguesController.getCatalogueRequestService().cancel();
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
        return executorService.submit(new SesameResolver(input)).get();
    }

    private UserInput getUserInput() {
        return new UserInput(inputText.getText(), radiusInput.getText(), radiusBox.getValue());
    }


    /**
     * Method that searches for results in boxes defined by RA and DEC.
     * According to MAST, it is possible to search for results in range of RA/DEC values e.g.: 30.1..30.5 will search
     * for all values from 30.1 to 30.5.
     * In order to have boxes of same area, it is necessary to create a grid that contains 7 points. Grid is then
     * divided into four boxes, in which recursive call happens.
     *
     * The easiest way to visualize it is XY plane where X = RA and Y = DEC and user input (point on plane) is at the
     * center of XY plane (mid-point).
     * Another 6 points are then created when radius is added/subtracted with respect to mid-point. A single box within
     * grid is then defined by two points that are diagonally opposite.
     *
     * @param coordinatesMin the lowest point on plane with the smallest RA and DEC
     * @param coordinatesMax the highest point on plane with the highest RA and DEC
     * @param radius length of which boxes are shortened, parallel to X or Y axis
     * @param catalogues catalogues to query
     * @param depth current depth of recursion
     * @return list of strings that contain output data
     * @throws RecursionDepthException if recursion maximum depth is reached
     */
    private List<String> mastGridSearch(Coordinates coordinatesMin, Coordinates coordinatesMax,
                                        double radius, List<Catalogue> catalogues, int depth) throws RecursionDepthException {
        if (depth == 10) {
            throw new RecursionDepthException();
        }

        List<String> output = new ArrayList<>();
        var service = new MastService();

        //Creating mid-point. It is clear that mid-point is the lowest point with added radius to RA and DEC values
        var coordinatesMid = new Coordinates(coordinatesMin);
        coordinatesMid.offsetRaDec(radius);

        //Creating rest of points. Each point is created with respect to mid-point.
        var coordinatesLeftMid = new Coordinates(coordinatesMid);
        coordinatesLeftMid.offsetRa(-radius);
        var coordinatesRightMid = new Coordinates(coordinatesMid);
        coordinatesRightMid.offsetRa(radius);
        var coordinatesUpMid = new Coordinates(coordinatesMid);
        coordinatesUpMid.offsetDec(radius);
        var coordinatesDownMid = new Coordinates(coordinatesMid);
        coordinatesDownMid.offsetDec(-radius);

        //For each box defined by two points, try query and if failed, call itself with smaller radius.
        try {
            output.add(service.sendRequest(service.createDataRequest(catalogues, coordinatesMin, coordinatesMid, MastServer.MAST_DEFAULT).get(0), true));
        } catch (CatalogueQueryException | TimeoutQueryException ex) {
            output.addAll(mastGridSearch(coordinatesMin, coordinatesMid, radius / 2, catalogues, depth + 1));
        }

        try {
            output.add(service.sendRequest(service.createDataRequest(catalogues, coordinatesLeftMid, coordinatesUpMid, MastServer.MAST_DEFAULT).get(0), true));
        } catch (CatalogueQueryException | TimeoutQueryException ex) {
            output.addAll(mastGridSearch(coordinatesLeftMid, coordinatesUpMid, radius / 2, catalogues, depth + 1));
        }

        try {
            output.add(service.sendRequest(service.createDataRequest(catalogues, coordinatesDownMid, coordinatesRightMid, MastServer.MAST_DEFAULT).get(0), true));
        } catch (CatalogueQueryException | TimeoutQueryException ex) {
            output.addAll(mastGridSearch(coordinatesDownMid, coordinatesRightMid, radius / 2, catalogues, depth + 1));
        }

        try {
            output.add(service.sendRequest(service.createDataRequest(catalogues, coordinatesMid, coordinatesMax, MastServer.MAST_DEFAULT).get(0), true));
        } catch (CatalogueQueryException | TimeoutQueryException ex) {
            output.addAll(mastGridSearch(coordinatesMid, coordinatesMax, radius / 2, catalogues, depth + 1));
        }

        return output;
    }

    /**
     * JavaFX Service, where search parameters are retrieved and where search action takes place.
     */
    private final Service<List<List<String>>> searchService = new Service<>() {
        @Override
        protected Task<List<List<String>>> createTask() {
            return new Task<>() {
                @Override
                protected List<List<String>> call() throws InterruptedException, ExecutionException {
                    List<Callable<List<String>>> tasks = new ArrayList<>();

                    Platform.runLater(() -> infoLabel.setText("Resolving input.."));
                    Coordinates resolvedInput = null;

                    //Resolving user input into coordinates into decimal degree notation
                    try {
                        resolvedInput = getResolvedInput(inputText.getText());
                    } catch (ExecutionException | InterruptedException ex) {
                        if (ex.getCause() instanceof ResolverQueryException) {
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.WARNING);
                                alert.setTitle("Input");
                                alert.setContentText("Input \"" + inputText.getText() + "\" could not be resolved.");
                                alert.showAndWait();
                            });
                        }
                    }

                    //If VizieR button was activated
                    if (vizierSearch) {
                        tasks.add(new GetDataTask<>(getVizierCatalogues(),
                                inputText.getText(), radiusInput.getText(), radiusBox.getValue(), VizierService.class,
                                getVizierServer(), false));
                    }

                    //If SIMBAD button was activated
                    if (simbadSearch && resolvedInput != null) {
                        String coordInput = resolvedInput.getRa() + " " + resolvedInput.getDec();
                        tasks.add(new GetDataTask<>(null,
                                coordInput, radiusInput.getText(), radiusBox.getValue(),
                                SimbadService.class, getSimbadServer(), false));
                    }

                    Platform.runLater(() -> infoLabel.setText("Downloading data.."));
                    var responses = executorService.invokeAll(tasks); //start Vizier and Simbad tasks
                    var output = new ArrayList<List<String>>();

                    //Because MAST can have nested queries, it is when all searches from Vizier and Simbad are done
                    if (mastSearch) {

                        //Create a catalogue for each MAST mission
                        var catalogues = new ArrayList<Catalogue>();
                        for (var mission : getMastMissions()) {
                            var catalogue = new Catalogue();
                            catalogue.addTable(mission);
                            catalogues.add(catalogue);
                        }

                        /*
                        For each catalogue then try to query.
                        If there is a timeout, grid search is performed.
                         */
                        for (var catalogue : catalogues) {
                            boolean mastTimeout = false;
                            var tempCatList = new ArrayList<Catalogue>();
                            tempCatList.add(catalogue);
                            try {
                                output.add(executorService.submit(new GetDataTask<>(tempCatList,
                                        inputText.getText(), radiusInput.getText(), radiusBox.getValue(), MastService.class,
                                        MastServer.MAST_DEFAULT, true)).get());
                            }
                            catch (ExecutionException | InterruptedException ex) {
                                if (ex.getCause() instanceof TimeoutQueryException) {
                                    mastTimeout = true;
                                    log.error("MAST timeout for \"" + catalogue.getTables().get(0).getName() + "\"");
                                }
                            }

                            if (mastTimeout && resolvedInput != null) {
                                /*
                                Radius input is always in arcmin (default by MAST), but resolved coordinates are
                                in decimal degrees. Transformation is needed => 1 degree equals to 60 arcmin
                                 */
                                var radius = Double.parseDouble(radiusInput.getText()) / 60;
                                var coordinatesMin = new Coordinates(resolvedInput);
                                var coordinatesMax = new Coordinates(resolvedInput);
                                coordinatesMin.offsetRaDec(-radius); //lowest point
                                coordinatesMax.offsetRaDec(radius); //highest point

                                try {
                                    output.add(mastGridSearch(coordinatesMin, coordinatesMax, radius, tempCatList, 0));
                                } catch (RecursionDepthException ex) {
                                    Platform.runLater(() -> {
                                        Alert alert = new Alert(Alert.AlertType.WARNING);
                                        alert.setTitle("Can't get results");
                                        alert.setContentText("Catalogue \"" + catalogue.getTables().get(0).getName() +
                                                "\" could not be queried anymore, because maximum recursion depth" +
                                                " happened. Try smaller radius or contact MAST.");
                                        alert.showAndWait();
                                    });
                                }
                            }
                        }
                    }

                    if (responses.isEmpty() && output.isEmpty()) {
                        searchService.cancel();
                    }

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
}
