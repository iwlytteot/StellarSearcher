package utils;

import controller.http.mast.MastService;
import controller.task.GetDataTask;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import model.Catalogue;
import model.Coordinates;
import model.Radius;
import model.Table;
import model.exception.RecursionDepthException;
import model.exception.TimeoutQueryException;
import model.mirror.MastServer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Data
@Slf4j
public class MastSearch {
    private final GridSearch gridSearcher;
    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public List<String> start(List<Table> missions, String input, String radiusInput, Radius radiusType,
                              Coordinates resolvedInput) throws RecursionDepthException {

        List<String> output = new ArrayList<>();

        //Create a catalogue for each MAST mission
        var catalogues = new ArrayList<Catalogue>();
        for (var mission : missions) {
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
                output.addAll(executorService.submit(new GetDataTask<>(tempCatList,
                        input, radiusInput, radiusType, MastService.class,
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
                var radius = Double.parseDouble(radiusInput) / 60;
                var coordinatesMin = new Coordinates(resolvedInput);
                var coordinatesMax = new Coordinates(resolvedInput);
                coordinatesMin.offsetRaDec(-radius); //lowest point
                coordinatesMax.offsetRaDec(radius); //highest point

                output.addAll(gridSearcher.start(coordinatesMin, resolvedInput, coordinatesMax, tempCatList, 0));
            }
        }
        return output;
    }
}
