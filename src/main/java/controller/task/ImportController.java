package controller.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import controller.http.Request;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import model.*;
import model.exception.CatalogueQueryException;
import model.exception.RecursionDepthException;
import model.exception.ResolverQueryException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

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
@Component
@Data
@Slf4j
public class ImportController {
    private final Searcher searcher;
    private final Request vizierService;
    private final Request simbadService;
    private final MastSearch mastSearcher;

    private final SesameResolver sesameResolver;

    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     * Method that parses JSON file from absolute path.
     * @return HashMap, where key is UserInput and value is List of Strings, which are basically
     * responses from respective servers.
     */
    @Async
    public CompletableFuture<HashMap<UserInput, List<String>>> start(String absolutePath, String vizierServer, String simbadServer) throws CatalogueQueryException {
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
                var tempMap = new HashMap<UserInput, List<CompletableFuture<List<String>>>>();

                for (var position : input.getInput()) {

                    Coordinates resolvedInput = null;

                    //Resolving user input into coordinates into decimal degree notation
                    try {
                        resolvedInput = sesameResolver.start(position).get();
                    } catch (ExecutionException | InterruptedException ex) {
                        if (ex.getCause() instanceof ResolverQueryException) {
                            log.error("Couldn't resolve input: " + position);
                        }
                    }

                    //Retrieves inputs and radius + radius type
                    var userInput = new UserInput(position, input.getRadius(), input.getUnit());

                    tempMap.put(userInput, new ArrayList<>());

                    //Vizier task
                    var vizierCatalogues = new ArrayList<Catalogue>();
                    vizierCatalogues.add(vizierCatalogue);
                    tempMap.get(userInput).add(searcher.start(vizierService, vizierCatalogues,
                            position, input.getRadius(), input.getUnit(), vizierServer, false));

                    //Simbad task
                    if (input.isSimbad() && resolvedInput != null) {
                        String coordInput = resolvedInput.getRa() + " " + resolvedInput.getDec();
                        tempMap.get(userInput).add(searcher.start(simbadService, null,
                                coordInput, input.getRadius(), input.getUnit(), simbadServer, false));
                    }

                    //Mast task
                    tempMap.get(userInput).add(mastSearcher.start(mastMissions, position, input.getRadius(),
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
        return CompletableFuture.completedFuture(output);
    }
}
