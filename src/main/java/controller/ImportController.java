package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import controller.http.GetDataTask;
import controller.http.SesameResolver;
import controller.http.mast.MastService;
import controller.http.simbad.SimbadService;
import controller.http.vizier.VizierService;
import model.Catalogue;
import model.InputDataCollector;
import model.Table;
import model.UserInput;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

@Component
public class ImportController {

    public HashMap<UserInput, List<String>> process(String absolutePath) {
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
                        var resolverTask = new FutureTask<>(new SesameResolver(position));
                        new Thread(resolverTask).start();
                        var simbadFutureTask = new FutureTask<>(new GetDataTask<>(null,
                                resolverTask.get(), input.getRadius(), input.getUnit(), SimbadService.class));
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

        return output;
    }
}
