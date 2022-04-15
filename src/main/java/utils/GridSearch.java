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
     * In order to have boxes of same area (note that it is possible to get different areas, because there can be
     * rectangles), it is necessary to create a grid that contains 7 points. Grid is then divided into four boxes,
     * in which recursive call happens.
     *
     * The easiest way to visualize it is XY plane where X-axis = <0, 360> for RA and Y-axis = <-90, 90> for DEC.
     * Mid-point (user's input) is at X = RA and Y = DEC.
     * Another 6 points are then created when radius is added/subtracted with respect to mid-point. A single box within
     * grid is then defined by two points that are diagonally opposite.
     *
     * @param coordinatesMin the lowest point on plane with the smallest RA and DEC
     * @param coordinatesMax the highest point on plane with the highest RA and DEC
     * @param coordinatesMid mid-point on plane
     * @param catalogues catalogues to query
     * @param depth current depth of recursion
     * @return list of strings that contain output data
     * @throws RecursionDepthException if recursion maximum depth is reached
     */
    public List<String> start(Coordinates coordinatesMin, Coordinates coordinatesMid, Coordinates coordinatesMax,
                              List<Catalogue> catalogues, int depth) throws RecursionDepthException {
        if (depth == 10) {
            throw new RecursionDepthException();
        }

        List<String> output = new ArrayList<>();
        var service = new MastService();

        /*
        It is necessary to obtain both RA and DEC radius's for left and right part of planes, that is divided by mid-point.
        It is because RA is of <0, 360> and DEC is of <-90, 90>, e.g.: input, where RA = 1 and DEC = 10 and radius = 3,
        hence it's impossible to get RA = -2. Therefore, rectangle boxes are created on
        left side => RA = 0 to 1, but DEC = 7 to 13

        and squares are created on
        right side => RA = 1 to 4 and DEC = 7 to 13

        (Same principle applies to DEC, but vertically as DEC lies on Y-axis)
         */
        var leftRaRadius = Math.abs(coordinatesMid.getRa() - coordinatesMin.getRa());
        var rightRaRadius = Math.abs(coordinatesMax.getRa() - coordinatesMid.getRa());
        var downDecRadius = Math.abs(coordinatesMid.getDec() - coordinatesMin.getDec());
        var upDecRadius = Math.abs(coordinatesMax.getDec() - coordinatesMid.getDec());

        //Creating rest of points. Each point is created with respect to mid-point.
        var coordinatesLeftMid = new Coordinates(coordinatesMid);
        coordinatesLeftMid.offsetRa(-leftRaRadius);
        var coordinatesRightMid = new Coordinates(coordinatesMid);
        coordinatesRightMid.offsetRa(rightRaRadius);
        var coordinatesUpMid = new Coordinates(coordinatesMid);
        coordinatesUpMid.offsetDec(upDecRadius);
        var coordinatesDownMid = new Coordinates(coordinatesMid);
        coordinatesDownMid.offsetDec(-downDecRadius);

        //For each box defined by two points, try query and if failed, call itself with smaller radius.
        try {
            output.add(service.sendRequest(service.createDataRequest(catalogues, coordinatesMin, coordinatesMid, MastServer.MAST_DEFAULT).get(0), true));
        } catch (CatalogueQueryException | TimeoutQueryException ex) {
            var midPoint = new Coordinates(coordinatesMid);
            midPoint.offsetRa(-leftRaRadius / 2);
            midPoint.offsetDec(-downDecRadius / 2);
            output.addAll(start(coordinatesMin, midPoint, coordinatesMid, catalogues, depth + 1));
        }

        try {
            output.add(service.sendRequest(service.createDataRequest(catalogues, coordinatesLeftMid, coordinatesUpMid, MastServer.MAST_DEFAULT).get(0), true));
        } catch (CatalogueQueryException | TimeoutQueryException ex) {
            var midPoint = new Coordinates(coordinatesMid);
            midPoint.offsetRa(-leftRaRadius / 2);
            midPoint.offsetDec(upDecRadius / 2);
            output.addAll(start(coordinatesLeftMid, midPoint, coordinatesUpMid, catalogues, depth + 1));
        }

        try {
            output.add(service.sendRequest(service.createDataRequest(catalogues, coordinatesDownMid, coordinatesRightMid, MastServer.MAST_DEFAULT).get(0), true));
        } catch (CatalogueQueryException | TimeoutQueryException ex) {
            var midPoint = new Coordinates(coordinatesMid);
            midPoint.offsetRa(rightRaRadius / 2);
            midPoint.offsetDec(-downDecRadius / 2);
            output.addAll(start(coordinatesDownMid, midPoint, coordinatesRightMid, catalogues, depth + 1));
        }

        try {
            output.add(service.sendRequest(service.createDataRequest(catalogues, coordinatesMid, coordinatesMax, MastServer.MAST_DEFAULT).get(0), true));
        } catch (CatalogueQueryException | TimeoutQueryException ex) {
            var midPoint = new Coordinates(coordinatesMid);
            midPoint.offsetRa(rightRaRadius / 2);
            midPoint.offsetDec(upDecRadius / 2);
            output.addAll(start(coordinatesMid, midPoint, coordinatesMax, catalogues, depth + 1));
        }

        return output;
    }
}
