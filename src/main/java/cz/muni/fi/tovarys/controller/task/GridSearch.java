package cz.muni.fi.tovarys.controller.task;

import cz.muni.fi.tovarys.controller.http.MastService;
import cz.muni.fi.tovarys.model.Catalogue;
import cz.muni.fi.tovarys.model.Coordinates;
import cz.muni.fi.tovarys.model.exception.RecursionDepthException;
import cz.muni.fi.tovarys.model.exception.TimeoutQueryException;
import cz.muni.fi.tovarys.model.mirror.MastServer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class GridSearch {
    /**
     * MAST catalogue ONLY
     * Method that searches for results in boxes defined by RA and DEC.
     * According to MAST, it is possible to search for results in range of RA/DEC values e.g.: 30.1..30.5 will search
     * for all values from 30.1 to 30.5.
     * In order to have boxes of same area, it is necessary to create a grid that contains 7 points.
     * Grid is then divided into four boxes, in which recursive call happens.
     *
     * The easiest way to visualize it is XY plane where X-axis = <0, 360> for RA and Y-axis = <-90, 90> for DEC.
     * Mid-point (user's input) is at X = RA and Y = DEC.
     * Another 6 points are then created when radius is added/subtracted with respect to mid-point. A single box within
     * grid is then defined by two points that are diagonally opposite.
     *
     * @param midPoint point where search begins
     * @param radius radius of search
     * @param catalogues catalogues to query
     * @param depth current depth of recursion
     * @return list of strings that contain output data
     * @throws RecursionDepthException if recursion maximum depth is reached
     */
    public List<String> start(Coordinates midPoint, double radius, List<Catalogue> catalogues, int depth)
            throws RecursionDepthException {
        if (depth == 3) {
            throw new RecursionDepthException();
        }

        List<String> output = new ArrayList<>();
        var service = new MastService();

        //For each box defined by two points, try query and if failed, call itself with smaller radius.

        try {
            output.add(service.sendRequest(service.createDataRequest(catalogues,
                    new Coordinates(midPoint.getRa() - radius, midPoint.getDec() - radius),
                    midPoint,
                    MastServer.MAST_DEFAULT).get(0)).get());
        } catch (ExecutionException | InterruptedException e) {
            if (e.getCause() instanceof TimeoutQueryException) {
                var newMid = new Coordinates(midPoint);
                newMid.offsetRaDec(-radius / 2);
                output.addAll(start(newMid, radius/2, catalogues, depth + 1));
            }
        }

        try {
            output.add(service.sendRequest(service.createDataRequest(catalogues,
                    new Coordinates(midPoint.getRa() - radius, midPoint.getDec()),
                    new Coordinates(midPoint.getRa(), midPoint.getDec() + radius),
                    MastServer.MAST_DEFAULT).get(0)).get());
        } catch (ExecutionException | InterruptedException e) {
            if (e.getCause() instanceof TimeoutQueryException) {
                var newMid = new Coordinates(midPoint);
                newMid.offsetRa(-radius / 2);
                newMid.offsetDec(radius / 2);
                output.addAll(start(newMid, radius/2, catalogues, depth + 1));
            }
        }

        try {
            output.add(service.sendRequest(service.createDataRequest(catalogues,
                    midPoint,
                    new Coordinates(midPoint.getRa() + radius, midPoint.getDec() + radius),
                    MastServer.MAST_DEFAULT).get(0)).get());
        } catch (ExecutionException | InterruptedException e) {
            if (e.getCause() instanceof TimeoutQueryException) {
                var newMid = new Coordinates(midPoint);
                newMid.offsetRa(radius / 2);
                newMid.offsetDec(radius / 2);
                output.addAll(start(newMid, radius/2, catalogues, depth + 1));
            }
        }

        try {
            output.add(service.sendRequest(service.createDataRequest(catalogues,
                    new Coordinates(midPoint.getRa(), midPoint.getDec() - radius),
                    new Coordinates(midPoint.getRa() + radius, midPoint.getDec()),
                    MastServer.MAST_DEFAULT).get(0)).get());
        } catch (ExecutionException | InterruptedException e) {
            if (e.getCause() instanceof TimeoutQueryException) {
                var newMid = new Coordinates(midPoint);
                newMid.offsetRa(radius / 2);
                newMid.offsetDec(-radius / 2);
                output.addAll(start(newMid, radius/2, catalogues, depth + 1));
            }
        }

        return output;
    }
}
