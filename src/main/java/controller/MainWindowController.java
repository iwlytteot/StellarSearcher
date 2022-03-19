package controller;

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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Class controller for "MainWindow.fxml". Beware that this controller is the main controller of the whole
 * application.
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

    private final VizierCataloguesController vizierCataloguesController;
    private final MastMissionController mastMissionController;
    private final ResultWindowController resultWindowController;

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

    private String getResolvedInput(String input) throws ExecutionException, InterruptedException {
        var resolverTask = new FutureTask<>(new SesameResolver(input));
        new Thread(resolverTask).start();
        return resolverTask.get();
    }

    private UserInput getUserInput() {
        return new UserInput(inputText.getText(), radiusInput.getText(), radiusBox.getValue());
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
                    List<Future<List<String>>> results = new ArrayList<>();

                    //If MAST button was activated
                    if (mastSearch) {
                        var catalogue = new Catalogue();
                        catalogue.setTables(getMastMissions());
                        var catalogues = new ArrayList<Catalogue>();
                        catalogues.add(catalogue);
                        results.add(executorCompletionService.submit(new GetDataTask<>(catalogues,
                                inputText.getText(), radiusInput.getText(), radiusBox.getValue(), MastService.class)));
                        ++threadCount;
                    }

                    //If VizieR button was activated
                    if (vizierSearch) {
                        results.add(executorCompletionService.submit(new GetDataTask<>(getVizierCatalogues(),
                                inputText.getText(), radiusInput.getText(), radiusBox.getValue(), VizierService.class)));
                        ++threadCount;
                    }

                    //If SIMBAD button was activated
                    if (simbadSearch) {
                        results.add(executorCompletionService.submit(new GetDataTask<>(null,
                                getResolvedInput(inputText.getText()), radiusInput.getText(), radiusBox.getValue(), SimbadService.class)));
                        ++threadCount;
                    }

                    //Waits for all threads to finish
                    for (int i = 0; i < threadCount; ++i) {
                        try {
                            executorCompletionService.take();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    //Gets results
                    var output = new ArrayList<List<String>>();
                    for (var result : results) {
                        output.add(result.get());
                    }
                    threadCount = 0;
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
            resultWindowController.fill(result);

            searchButton.getScene().setCursor(Cursor.DEFAULT);

            resultWindowEventHandler.getStage().show();
        }

        @Override
        protected void cancelled() {
            searchButton.getScene().setCursor(Cursor.DEFAULT);
            log.warn("Search action was cancelled, the input was: "
                    + inputText.getText() + ", " + radiusInput.getText() + " " + radiusBox.getValue());
        }

        @Override
        protected void failed() {
            searchButton.getScene().setCursor(Cursor.DEFAULT);
            log.error("Search action has failed, the input was: "
                    + inputText.getText() + ", " + radiusInput.getText() + " " + radiusBox.getValue());
        }
    };

    /**
     * Method that handles uppermost interaction with user when importing file.
     */
    public void importData() {
        FileChooser fileChooser = new FileChooser();
        var selectedFile = fileChooser.showOpenDialog(searchButton.getScene().getWindow());
        if (selectedFile == null) {
            return;
        }
        searchButton.getScene().setCursor(Cursor.WAIT);

        //Starts a new task for processing, so it doesn't block main JavaFX thread
        var importTask = new FutureTask<>(new ImportControllerTask(selectedFile.getAbsolutePath()));
        new Thread(importTask).start();
        HashMap<UserInput, List<String>> output = new HashMap<>();
        try {
            output = importTask.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while retrieving data from import: " + e.getMessage());
        }
        if (resultWindowEventHandler.getStage() == null) {
            context.publishEvent(new ResultWindowEvent(new Stage()));
        }
        resultWindowController.fill(output);
        searchButton.getScene().setCursor(Cursor.DEFAULT);

        resultWindowEventHandler.getStage().show();
    }
}
