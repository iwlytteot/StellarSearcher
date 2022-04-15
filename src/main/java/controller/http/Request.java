package controller.http;

import model.Catalogue;
import model.exception.CatalogueQueryException;
import model.Radius;
import model.exception.TimeoutQueryException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

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
    default String sendRequest(URI uri, boolean timeout) throws CatalogueQueryException, TimeoutQueryException {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(uri).GET().build();
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new CatalogueQueryException();
        }
    }
}
