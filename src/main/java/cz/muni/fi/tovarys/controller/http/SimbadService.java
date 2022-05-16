package cz.muni.fi.tovarys.controller.http;

import cz.muni.fi.tovarys.model.Catalogue;
import cz.muni.fi.tovarys.model.Radius;
import cz.muni.fi.tovarys.model.exception.CatalogueQueryException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for Simbad database. For official API, check http://simbad.u-strasbg.fr/guide/sim-url.htx.
 */
@Component
@Qualifier("simbadService")
public class SimbadService implements Request {
    private static final String BASE_PARAM = "/simbad/sim-coo?";
    private static final String SUFFIX_URL = "&output.max=999999&output.format=VOTable";

    @Override
    public List<URI> createDataRequest(List<Catalogue> catalogues, String identification, String radius,
                                       Radius radiusType, String baseUrl) {
        String params = "Coord=" + URLEncoder.encode(identification, StandardCharsets.UTF_8) +
                "&Radius.unit=" +
                radiusType.name.replace(" ", "") +
                "&" +
                "Radius=" +
                URLEncoder.encode(radius, StandardCharsets.UTF_8);

        var uri = URI.create(baseUrl + BASE_PARAM
                + params
                + SUFFIX_URL);
        var output = new ArrayList<URI>();
        output.add(uri);
        return output;
    }

    @Async
    @Override
    public CompletableFuture<String> sendRequest(URI uri) {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(uri).GET().build();
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return CompletableFuture.completedFuture(response.body());
        } catch (IOException | InterruptedException e) {
            return CompletableFuture.failedFuture(new CatalogueQueryException());
        }
    }
}
