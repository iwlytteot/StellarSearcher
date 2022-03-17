package controller.http;

import model.Catalogue;
import model.CatalogueQueryException;
import model.Radius;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public interface Request {
    /**
     * Creates URI that holds full web path to respective server and that
     * is immediately ready to use.
     * According to the ASU qualifications for VizieR, it is required to decode arguments,
     * more on https://cdsarc.u-strasbg.fr/doc/asu-summary.htx
     *
     * @param catalogues list of catalogues
     * @param coordinates coordinates
     * @param radius radius
     * @param radiusType type of radius
     * @return URI object
     */
    List<URI> createDataRequest(List<Catalogue> catalogues, String coordinates, String radius, Radius radiusType);

    /**
     * Sends request and returns a string with data.
     * @param uri with specified catalogues (tables), radius and parameters
     */
    default String sendRequest(URI uri) throws CatalogueQueryException {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(uri).GET().build();
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new CatalogueQueryException(e.getMessage());
        }
    }
}
