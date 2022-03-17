package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import controller.http.GetDataTask;
import controller.http.Request;
import controller.http.SesameResolver;
import controller.http.mast.MastService;
import controller.http.simbad.SimbadService;
import controller.http.vizier.VizierService;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.*;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import view.event.MastWindowEvent;
import view.event.ResultWindowEvent;
import view.event.VizierWindowEvent;
import view.handler.MastWindowEventHandler;
import view.handler.ResultWindowEventHandler;
import view.handler.VizierWindowEventHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
@ComponentScan("model")
@FxmlView("/MainWindow.fxml")
public class MainWindowController {
    private final ConfigurableApplicationContext context;
    private final VizierWindowEventHandler vizierWindowEventHandler;
    private final MastWindowEventHandler mastWindowEventHandler;
    private final ResultWindowEventHandler resultWindowEventHandler;

    private final VizierCataloguesController vizierCataloguesController;
    private final MastMissionController mastMissionController;
    private final ResultWindowController resultWindowController;

    private final SesameResolver sesameResolver;
    private final OutputData outputData;

    private final VizierService vizierService;
    private final MastService mastService;
    private final SimbadService simbadService;

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

    private boolean vizierSearch = false, simbadSearch = false, mastSearch = false;
    private final List<String> affectedTables = Collections.synchronizedList(new ArrayList<>());

    private final ExecutorService executorWrapper = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final ExecutorCompletionService<List<String>> executorCompletionService = new ExecutorCompletionService<>(executorWrapper);
    private int threadCount = 0;

    public MainWindowController(ConfigurableApplicationContext context, VizierWindowEventHandler vizierWindowEventHandler,
                                MastWindowEventHandler mastWindowEventHandler, ResultWindowEventHandler resultWindowEventHandler,
                                VizierCataloguesController vizierCataloguesController, MastMissionController mastMissionController,
                                ResultWindowController resultWindowController, SesameResolver sesameResolver, OutputData outputData,
                                VizierService vizierService, MastService mastService, SimbadService simbadService) {
        this.context = context;
        this.vizierWindowEventHandler = vizierWindowEventHandler;
        this.mastWindowEventHandler = mastWindowEventHandler;
        this.resultWindowEventHandler = resultWindowEventHandler;
        this.vizierCataloguesController = vizierCataloguesController;
        this.mastMissionController = mastMissionController;
        this.resultWindowController = resultWindowController;
        this.sesameResolver = sesameResolver;
        this.outputData = outputData;
        this.vizierService = vizierService;
        this.mastService = mastService;
        this.simbadService = simbadService;
    }

    @FXML
    public void initialize() {
        radiusBox.getItems().setAll(Radius.values());
        radiusBox.getSelectionModel().select(Radius.ARCMIN);

        vizierTableButton.setDisable(true);
        mastTableButton.setDisable(true);
        searchButton.setDisable(true);
    }

    public void exit() {
        executorWrapper.shutdownNow();
    }

    public void enterVizier(MouseEvent mouseEvent) {
        vizierSearch = !vizierSearch;
        rectLeft.setFill(vizierSearch ? Color.GREEN : Color.RED);
        vizierTableButton.setDisable(!vizierSearch);
        SearchButtonCheck();
    }

    public void enterSimbad(MouseEvent mouseEvent) {
        simbadSearch = !simbadSearch;
        rectMid.setFill(simbadSearch ? Color.GREEN : Color.RED);
        SearchButtonCheck();
    }

    public void enterMast(MouseEvent mouseEvent) {
        mastSearch = !mastSearch;
        rectRight.setFill(mastSearch ? Color.GREEN : Color.RED);
        mastTableButton.setDisable(!mastSearch);
        SearchButtonCheck();
    }



    public void mastTableButtonAction(ActionEvent actionEvent) {
        if (mastWindowEventHandler.getStage() == null) {
            context.publishEvent(new MastWindowEvent(new Stage()));
        }
        mastWindowEventHandler.getStage().show();
    }

    public void vizierTableButtonAction(ActionEvent actionEvent) {
        if (vizierWindowEventHandler.getStage() == null) {
            context.publishEvent(new VizierWindowEvent(new Stage()));
        }
        vizierWindowEventHandler.getStage().show();
    }

    private void SearchButtonCheck() {
        searchButton.setDisable(!mastSearch && !simbadSearch && !vizierSearch);
    }

    public void searchAction(ActionEvent actionEvent) {
        if (inputText.getText().isEmpty() || radiusInput.getText().isEmpty()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Missing input");
                alert.setContentText("Missing input");
                alert.showAndWait();
            });
            return;
        }
        try {
            var radius= Integer.parseInt(radiusInput.getText());
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
                alert.setTitle("Missing input");
                alert.setContentText("Missing input");
                alert.showAndWait();
            });
            return;
        }

        outputData.setInput(inputText.getText());
        outputData.setRadius(radiusInput.getText());
        outputData.setRadiusType(radiusBox.getValue().name);

        searchButton.getScene().setCursor(Cursor.WAIT);

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

    private String getResolvedInput(String input) {
        return sesameResolver.resolve(input);
    }

    private UserInput getUserInput() {
        return new UserInput(inputText.getText(), radiusInput.getText(), radiusBox.getValue());
    }

    private final Service<List<List<String>>> searchService = new Service<>() {
        @Override
        protected Task<List<List<String>>> createTask() {
            return new Task<>() {
                @Override
                protected List<List<String>> call() throws ExecutionException, InterruptedException {
                    List<Future<List<String>>> results = new ArrayList<>();
                    if (mastSearch) {
                        var catalogue = new Catalogue();
                        catalogue.setTables(getMastMissions());
                        var catalogues = new ArrayList<Catalogue>();
                        catalogues.add(catalogue);
                        results.add(executorCompletionService.submit(new GetDataTask<>(catalogues,
                                inputText.getText(), radiusInput.getText(), radiusBox.getValue(), MastService.class)));
                        ++threadCount;
                    }

                    if (vizierSearch) {
                        results.add(executorCompletionService.submit(new GetDataTask<>(getVizierCatalogues(),
                                inputText.getText(), radiusInput.getText(), radiusBox.getValue(), VizierService.class)));
                        ++threadCount;
                    }

                    if (simbadSearch) {
                        var resolvedInput = getResolvedInput(inputText.getText());
                        results.add(executorCompletionService.submit(new GetDataTask<>(null,
                                resolvedInput, radiusInput.getText(), radiusBox.getValue(), SimbadService.class)));
                        ++threadCount;
                    }

                    for (int i = 0; i < threadCount; ++i) {
                        try {
                            executorCompletionService.take();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    var output = new ArrayList<List<String>>();
                    for (var result : results) {
                        output.add(result.get());
                    }
                    threadCount = 0;
                    return output;
                }
            };
        }
        @Override
        protected void succeeded() {
            if (resultWindowEventHandler.getStage() == null) {
                context.publishEvent(new ResultWindowEvent(new Stage()));
            }
            var flatList = searchService.getValue().stream().flatMap(List::stream).collect(Collectors.toList());
            var result = new HashMap<UserInput, List<String>>();
            result.put(getUserInput(), flatList);
            resultWindowController.fill(result);

            searchButton.getScene().setCursor(Cursor.DEFAULT);

            resultWindowEventHandler.getStage().show();
        }

        @Override
        protected void cancelled() {
            searchButton.getScene().setCursor(Cursor.DEFAULT);
        }

        @Override
        protected void failed() {
            searchButton.getScene().setCursor(Cursor.DEFAULT);
        }
    };

    public void importData(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        var selectedFile = fileChooser.showOpenDialog(searchButton.getScene().getWindow());
        searchButton.getScene().setCursor(Cursor.WAIT);
        processData(selectedFile.getAbsolutePath());
    }

    private void processData(String absolutePath) {
        var output = new HashMap<UserInput, List<String>>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            InputDataCollector inputDataCollector = objectMapper.readValue(new File(absolutePath), InputDataCollector.class);
            for (var input : inputDataCollector.getTargets()) {
                //Vizier part
                var vizierCatalogue = new Catalogue();
                for (var object : input.getVizier()) {
                    vizierCatalogue.addTable(new Table(object));
                }

                //Mast part
                var mastCatalogue = new Catalogue();
                for (var object : input.getMast()) {
                    mastCatalogue.addTable(new Table(object));
                }

                //Search part
                var tempMap = new HashMap<UserInput, List<FutureTask<List<String>>>>();
                for (var position : input.getInput()) {
                    var userInput = new UserInput(position, input.getRadius(), input.getUnit());
                    tempMap.put(userInput, new ArrayList<>());

                    var vizierCatalogues = new ArrayList<Catalogue>();
                    vizierCatalogues.add(vizierCatalogue);
                    var vizierFutureTask = new FutureTask<>(new GetDataTask<>(vizierCatalogues,
                            position, input.getRadius(), input.getUnit(), VizierService.class));
                    new Thread(vizierFutureTask).start();
                    tempMap.get(userInput).add(vizierFutureTask);

                    var mastCatalogues = new ArrayList<Catalogue>();
                    mastCatalogues.add(mastCatalogue);
                    var mastFutureTask = new FutureTask<>(new GetDataTask<>(mastCatalogues,
                            position, input.getRadius(), input.getUnit(), MastService.class));
                    new Thread(mastFutureTask).start();
                    tempMap.get(userInput).add(mastFutureTask);

                    if (input.isSimbad()) {
                        var simbadFutureTask = new FutureTask<>(new GetDataTask<>(null,
                                getResolvedInput(position), input.getRadius(), input.getUnit(), SimbadService.class));
                        new Thread(simbadFutureTask).start();
                        tempMap.get(userInput).add(simbadFutureTask);
                    }
                }
                for (var entry : tempMap.entrySet()) {
                    var tempList = new ArrayList<List<String>>();
                    for (var futureTask : entry.getValue()) {
                        tempList.add(futureTask.get());
                    }
                    output.put(entry.getKey(), tempList.stream().flatMap(List::stream).collect(Collectors.toList()));
                }
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        if (resultWindowEventHandler.getStage() == null) {
            context.publishEvent(new ResultWindowEvent(new Stage()));
        }
        resultWindowController.fill(output);
        searchButton.getScene().setCursor(Cursor.DEFAULT);

        resultWindowEventHandler.getStage().show();
    }
}
