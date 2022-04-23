package controller.http;

import controller.http.Request;
import model.Catalogue;
import model.Coordinates;
import model.Radius;
import model.exception.CatalogueQueryException;
import model.exception.TimeoutQueryException;
import model.mirror.MastServer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for MAST catalogue. For official API, check https://archive.stsci.edu/vo/mast_services.html.
 */
@Component
@Qualifier("mastService")
public class MastService implements Request {
    private static final String BASE_PARAMS = "search.php?action=Search&outputformat=VOTable&max_records=999999";

    @Override
    public List<URI> createDataRequest(List<Catalogue> catalogues, String identification, String radius,
                                       Radius radiusType, String baseUrl) {
        var output = new ArrayList<URI>();
        StringBuilder base = new StringBuilder();

        base.append("&radius=");
        base.append(URLEncoder.encode(radius, StandardCharsets.UTF_8));
        base.append("&target=");
        base.append(URLEncoder.encode(identification, StandardCharsets.UTF_8));

        return getUris(catalogues, baseUrl, output, base);
    }

    @Async
    @Override
    public CompletableFuture<String> sendRequest(URI uri) {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(uri).GET().timeout(Duration.ofSeconds(MastServer.TIMEOUT_LIMIT)).build();
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return CompletableFuture.completedFuture(response.body());
        } catch (InterruptedException | IOException ex) {
            if (ex.getMessage().contains("timed out")) {
                return CompletableFuture.failedFuture(new TimeoutQueryException());
            }
            return CompletableFuture.failedFuture(new CatalogueQueryException());
        }
    }

    /**
     * Method that creates request for RA and DEC ranges.
     * @param catalogues list of catalogues for searching
     * @param coordinatesMin the lowest coordinate
     * @param coordinatesMax the highest coordinate
     * @param baseUrl path to queried server
     * @return List of URIs
     */
    public List<URI> createDataRequest(List<Catalogue> catalogues, Coordinates coordinatesMin,
                                       Coordinates coordinatesMax, String baseUrl) {
        var output = new ArrayList<URI>();
        StringBuilder base = new StringBuilder();

        base.append("&RA=");
        base.append(URLEncoder.encode(coordinatesMin.getRa() + ".." + coordinatesMax.getRa(), StandardCharsets.UTF_8));
        base.append("&DEC=");
        base.append(URLEncoder.encode(coordinatesMin.getDec() + ".." + coordinatesMax.getDec(), StandardCharsets.UTF_8));

        return getUris(catalogues, baseUrl, output, base);
    }

    /**
     * Helper method for building arguments for the rest of URI address
     * @param catalogues list of catalogues for searching
     * @param baseUrl path to queried server
     * @param output list to be appended to
     * @param base first part of URI
     * @return List of URIs
     */
    private List<URI> getUris(List<Catalogue> catalogues, String baseUrl, List<URI> output, StringBuilder base) {
        for (var table : catalogues.get(0).getTables()) {
            String source = table.getName() + "/";
            StringBuilder params = new StringBuilder();
            table.getColumns().forEach((k, v) -> params
                    .append(URLEncoder.encode(k, StandardCharsets.UTF_8))
                    .append("=")
                    .append(URLEncoder.encode(v, StandardCharsets.UTF_8))
                    .append("&"));
            output.add(URI.create(baseUrl
                    + source
                    + BASE_PARAMS
                    + base
                    + "&"
                    + params));
        }
        return output;
    }
}
