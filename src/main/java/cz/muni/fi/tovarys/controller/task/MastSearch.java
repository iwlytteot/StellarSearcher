package cz.muni.fi.tovarys.controller.task;

import cz.muni.fi.tovarys.controller.http.Request;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import cz.muni.fi.tovarys.model.Catalogue;
import cz.muni.fi.tovarys.model.Coordinates;
import cz.muni.fi.tovarys.model.Radius;
import cz.muni.fi.tovarys.model.Table;
import cz.muni.fi.tovarys.model.exception.OutOfRangeException;
import cz.muni.fi.tovarys.model.exception.RecursionDepthException;
import cz.muni.fi.tovarys.model.exception.TimeoutQueryException;
import cz.muni.fi.tovarys.model.mirror.MastServer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Class to wrap search at MAST. The search is different from Vizier and Simbad. In case of timeout, the grid
 * search is performed.
 */
@Component
@Data
@Slf4j
public class MastSearch {
    private final Searcher searcher;
    private final GridSearch gridSearcher;
    private final Request mastService;

    /**
     * Asynchronous method that wraps searching in MAST into two parts: classic one with radius and grid search. Latter is executed
     * if first one fails due to timeout.
     * @param missions list of missions (tables) that should be queried
     * @param input user input
     * @param radiusInput user radius input
     * @param radiusType type of radius
     * @param resolvedInput resolved input of type Coordinates. Needed if grid search is performed
     * @return CompletableFuture of type List<String>. List holds results from MAST server or exceptions
     * RecursionDepthException if recursion depth is exceeded or CatalogueQueryException if error happened during querying
     */
    @Async
    public CompletableFuture<List<String>> start(List<Table> missions, String input, String radiusInput, Radius radiusType,
                                                 Coordinates resolvedInput) {

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
                output.addAll(searcher.start(mastService, tempCatList,
                        input, radiusInput, radiusType,
                        MastServer.MAST_DEFAULT).get());
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
                if (resolvedInput.getDec() + radius > 90 || resolvedInput.getDec() - radius < -90
                        || resolvedInput.getRa() + radius > 360 || resolvedInput.getRa() - radius < 0) {
                    return CompletableFuture.failedFuture(new OutOfRangeException());
                }

                try {
                    output.addAll(gridSearcher.start(resolvedInput, radius, tempCatList, 0));
                } catch (RecursionDepthException e) {
                    return CompletableFuture.failedFuture(new RecursionDepthException());
                }
            }
        }
        return CompletableFuture.completedFuture(output);
    }
}
