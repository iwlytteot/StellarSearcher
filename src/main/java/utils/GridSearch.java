package utils;

import controller.http.mast.MastService;
import model.Catalogue;
import model.Coordinates;
import model.exception.CatalogueQueryException;
import model.exception.RecursionDepthException;
import model.exception.TimeoutQueryException;
import model.mirror.MastServer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GridSearch {
    /**
     * MAST catalogue ONLY
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
    public List<String> start(Coordinates coordinatesMin, Coordinates coordinatesMax,
                              double radius, List<Catalogue> catalogues, int depth) throws RecursionDepthException {
        if (depth == 10) {
            throw new RecursionDepthException();
        }

        System.out.println("DEPTH: " + depth);

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
            output.addAll(start(coordinatesMin, coordinatesMid, radius / 2, catalogues, depth + 1));
        }

        try {
            output.add(service.sendRequest(service.createDataRequest(catalogues, coordinatesLeftMid, coordinatesUpMid, MastServer.MAST_DEFAULT).get(0), true));
        } catch (CatalogueQueryException | TimeoutQueryException ex) {
            output.addAll(start(coordinatesLeftMid, coordinatesUpMid, radius / 2, catalogues, depth + 1));
        }

        try {
            output.add(service.sendRequest(service.createDataRequest(catalogues, coordinatesDownMid, coordinatesRightMid, MastServer.MAST_DEFAULT).get(0), true));
        } catch (CatalogueQueryException | TimeoutQueryException ex) {
            output.addAll(start(coordinatesDownMid, coordinatesRightMid, radius / 2, catalogues, depth + 1));
        }

        try {
            output.add(service.sendRequest(service.createDataRequest(catalogues, coordinatesMid, coordinatesMax, MastServer.MAST_DEFAULT).get(0), true));
        } catch (CatalogueQueryException | TimeoutQueryException ex) {
            output.addAll(start(coordinatesMid, coordinatesMax, radius / 2, catalogues, depth + 1));
        }

        return output;
    }
}
