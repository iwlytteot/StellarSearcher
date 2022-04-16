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
     * decode arguments, more on https://cdsarc.u-strasbg.fr/doc/asu-summary.htx. This applies to the rest of servers.
     *
     * @param catalogues list of catalogues for searching
     * @param identification identification of object. Can be either coordinates or named identification
     * @param radius radius
     * @param radiusType type of radius
     * @param server path to queried server
     * @return List of URIs
     */
    List<URI> createDataRequest(List<Catalogue> catalogues, String identification, String radius,
                                Radius radiusType, String server);

    /**
     * Asynchronous method that tries to send an HTTP GET request and returns a CompletableFuture object of type String.
     * @param uri with specified catalogues (tables), radius and parameters
     * @param timeout if timeout is needed to be set
     * @return CompletableFuture object of type String. If request is successful, then String represents
     * direct response from server. The response varies from HTML object to VOTable document.
     */
    CompletableFuture<String> sendRequest(URI uri, boolean timeout);
}
