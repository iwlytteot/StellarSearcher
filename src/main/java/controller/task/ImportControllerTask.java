package controller.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import controller.http.simbad.SimbadService;
import controller.http.vizier.VizierService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import model.*;
import model.exception.RecursionDepthException;
import model.exception.ResolverQueryException;
import utils.MastSearch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Class for handling and processing imported file in JSON format.
 */
@Data
@Slf4j
public class ImportControllerTask implements Callable<HashMap<UserInput, List<String>>> {
    private final String absolutePath;
    private final String vizierServer;
    private final String simbadServer;
    private final MastSearch mastSearcher;

    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     * Method that parses JSON file from absolute path.
     * @return HashMap, where key is UserInput and value is List of Strings, which are basically
     * responses from respective servers.
     */
    @Override
    public HashMap<UserInput, List<String>> call() {
        var output = new HashMap<UserInput, List<String>>();
        try {
            //Opens JSON file and parses it to InputDataCollector wrapper class
            ObjectMapper objectMapper = new ObjectMapper();
            InputDataCollector inputDataCollector = objectMapper.readValue(new File(absolutePath), InputDataCollector.class);

            for (var input : inputDataCollector.getTargets()) {
                //Retrieving input, Vizier part
                var vizierCatalogue = new Catalogue();
                for (var name : input.getVizier()) {
                    if (!name.isEmpty()) {
                        vizierCatalogue.addTable(new Table(name));
                    }
                }

                //Retrieving input, Mast part
                List<Table> mastMissions = new ArrayList<>();
                for (var name : input.getMast()) {
                    if (!name.isEmpty()) {
                        mastMissions.add(new Table(name));
                    }
                }

                //Search part
                var tempMap = new HashMap<UserInput, List<Future<List<String>>>>();
                var tempMastMap = new HashMap<UserInput, List<List<String>>>();

                for (var position : input.getInput()) {

                    Coordinates resolvedInput = null;

                    //Resolving user input into coordinates into decimal degree notation
                    try {
                        resolvedInput = executorService.submit(new SesameResolverTask(position)).get();
                    } catch (ExecutionException | InterruptedException ex) {
                        if (ex.getCause() instanceof ResolverQueryException) {
                            log.error("Couldn't resolve input: " + position);
                        }
                    }

                    //Retrieves inputs and radius + radius type
                    var userInput = new UserInput(position, input.getRadius(), input.getUnit());

                    tempMap.put(userInput, new ArrayList<>());
                    tempMastMap.put(userInput, new ArrayList<>());

                    //Vizier task
                    var vizierCatalogues = new ArrayList<Catalogue>();
                    vizierCatalogues.add(vizierCatalogue);
                    tempMap.get(userInput).add(executorService.submit(new GetDataTask<>(vizierCatalogues,
                            position, input.getRadius(), input.getUnit(), VizierService.class, vizierServer, false)));

                    //Simbad task
                    if (input.isSimbad() && resolvedInput != null) {
                        String coordInput = resolvedInput.getRa() + " " + resolvedInput.getDec();
                        tempMap.get(userInput).add(executorService.submit(new GetDataTask<>(null,
                                coordInput, input.getRadius(), input.getUnit(), SimbadService.class, simbadServer, false)));
                    }

                    //Mast task
                    tempMastMap.get(userInput).add(mastSearcher.start(mastMissions, position, input.getRadius(),
                            input.getUnit(), resolvedInput));
                }
                //Retrieving data from Future and associating user input with results so that Result
                //windows contains data that are in correct tab
                for (var entry : tempMap.entrySet()) {
                    var tempList = new ArrayList<List<String>>();
                    for (var future : entry.getValue()) {
                        tempList.add(future.get());
                    }

                    //in case there is same input and radius present (in different target object), then append to
                    //existing result
                    var newValue = output.getOrDefault(entry.getKey(), new ArrayList<>());

                    //flatting the result because it is not necessary to have deeper division in results
                    //as it is enough to divide it by input and radius
                    newValue.addAll(tempList.stream().flatMap(List::stream).collect(Collectors.toList()));

                    var mastOutput = tempMastMap.get(entry.getKey());
                    if (mastOutput != null) {
                        newValue.addAll(mastOutput.stream().flatMap(List::stream).collect(Collectors.toList()));
                    }
                    output.put(entry.getKey(), newValue);
                }
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            log.error("Error while processing JSON file: " + e.getMessage());
        } catch (RecursionDepthException e) {
            log.error("MAST could not be queried anymore, because maximum recursion depth happened. " +
                    "Try smaller radius or contact MAST");
        }
        executorService.shutdown();
        return output;
    }
}
