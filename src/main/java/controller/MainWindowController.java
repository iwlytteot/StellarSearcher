package controller;

import controller.http.SesameResolver;
import controller.http.mast.MastRequest;
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
import javafx.stage.Stage;
import model.Catalogue;
import model.Radius;
import model.OutputData;
import model.Table;
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
import java.util.List;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private final ExecutorCompletionService<Void> executorCompletionService = new ExecutorCompletionService<>(executorWrapper);
    private int threadCount = 0;

    public MainWindowController(ConfigurableApplicationContext context, VizierWindowEventHandler vizierWindowEventHandler,
                                MastWindowEventHandler mastWindowEventHandler, ResultWindowEventHandler resultWindowEventHandler,
                                VizierCataloguesController vizierCataloguesController, MastMissionController mastMissionController,
                                ResultWindowController resultWindowController, SesameResolver sesameResolver, OutputData outputData) {
        this.context = context;
        this.vizierWindowEventHandler = vizierWindowEventHandler;
        this.mastWindowEventHandler = mastWindowEventHandler;
        this.resultWindowEventHandler = resultWindowEventHandler;
        this.vizierCataloguesController = vizierCataloguesController;
        this.mastMissionController = mastMissionController;
        this.resultWindowController = resultWindowController;
        this.sesameResolver = sesameResolver;
        this.outputData = outputData;
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

    Runnable vizierTask = () -> {
        var vizierService = new VizierService();
        var catalogues = getVizierCatalogues();

        if (catalogues.isEmpty()) {
            return;
        }

        var requestURI = vizierService.createDataRequest(catalogues, inputText.getText(), radiusInput.getText(), radiusBox.getValue());
        vizierService.sendRequest(requestURI.get(0));

        synchronized (affectedTables) {
            affectedTables.add("vizier_data");
        }
    };

    Runnable mastTask = () -> {
        var mastService = new MastService();
        var catalogue = new Catalogue();
        catalogue.setTables(getMastMissions());
        if (catalogue.getTables().isEmpty()) {
            return;
        }
        var catalogues = new ArrayList<Catalogue>();
        catalogues.add(catalogue);
        var requests = mastService.createDataRequest(catalogues, inputText.getText(), radiusInput.getText(), radiusBox.getValue());

        for (var request : requests) {
            executorCompletionService.submit(new MastRequest(request), null);
            ++threadCount;
        }

        synchronized (affectedTables) {
            catalogue.getTables().forEach(t -> affectedTables.add(t.getName().replace("/", "_")));
        }
    };

    private String getResolvedInput(String input) {
        return sesameResolver.resolve(input);
    }

    Runnable simbadTask = () -> {
        var simbadService = new SimbadService();
        var resolvedInput = getResolvedInput(inputText.getText());

        var requestURI = simbadService.createDataRequest(null, resolvedInput, radiusInput.getText(), radiusBox.getValue());
        simbadService.sendRequest(requestURI.get(0));

        synchronized (affectedTables) {
            affectedTables.add("simbad_data");
        }
    };

    private final Service<Void> searchService = new Service<>() {
        @Override
        protected Task<Void> createTask() {
            return new Task<>() {
                @Override
                protected Void call() {
                    if (mastSearch) {
                        executorCompletionService.submit(mastTask, null);
                        ++threadCount;
                    }

                    if (vizierSearch) {
                        executorCompletionService.submit(vizierTask, null);
                        ++threadCount;
                    }

                    if (simbadSearch) {
                        executorCompletionService.submit(simbadTask, null);
                        ++threadCount;
                    }

                    for (int i = 0; i < threadCount; ++i) {
                        try {
                            executorCompletionService.take();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    threadCount = 0;
                    return null;
                }
            };
        }
        @Override
        protected void succeeded() {
            if (resultWindowEventHandler.getStage() == null) {
                context.publishEvent(new ResultWindowEvent(new Stage()));
            }
            resultWindowController.fill(affectedTables);
            affectedTables.clear();

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
}
