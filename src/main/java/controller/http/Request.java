package controller.http;

import model.Catalogue;
import model.Radius;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Request {
    /**
     * Creates List of URIs. These URIs hold full web path to respective server and are ready for immediate querying.
     * According to the ASU qualifications for VizieR (that recalls HTTP protocol specification), it is required to
     * decode arguments, more on https://cdsarc.u-strasbg.fr/doc/asu-summary.htx
     *
     * @param catalogues list of catalogues for searching
     * @param identification identification of object. Can be either coordinates or named identification
     * @param radius radius
     * @param radiusType type of radius
     * @param server base url of the server, can be a mirror
     * @return List of URIs
     */
    List<URI> createDataRequest(List<Catalogue> catalogues, String identification, String radius,
                                Radius radiusType, String server);

    /**
     * Sends HTTP GET request and returns a string with data.
     * @param uri with specified catalogues (tables), radius and parameters
     */
    CompletableFuture<String> sendRequest(URI uri, boolean timeout);
}
