package controller.http;

import model.Catalogue;
import model.Radius;

import java.net.URI;
import java.util.List;

public interface Request {
    /**
     * Creates URI that holds full web path to respective server and that
     * is immediately ready to use.
     * @param catalogues list of catalogues
     * @param coordinates coordinates
     * @param radius radius
     * @param radiusType type of radius
     * @return URI object
     */
    URI createDataRequest(List<Catalogue> catalogues, String coordinates, String radius, Radius radiusType);

    /**
     * Sends request and saves incoming data into "data.txt" file. Note that
     * this file is being reused; there is no need to keep copies of these files.
     * @param uri with specified catalogues (tables), radius and parameters
     */
    void sendRequest(URI uri);
}
