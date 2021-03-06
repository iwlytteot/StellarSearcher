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
 * Service for VizieR catalogue. For official API, check https://cdsarc.u-strasbg.fr/doc/asu-summary.htx.
 */
@Component
@Qualifier("vizierService")
public class VizierService implements Request {
    private static final String BASE_PARAM = "/viz-bin/votable?";

    @Override
    public List<URI> createDataRequest(List<Catalogue> catalogues, String identification, String radius,
                                       Radius radiusType, String baseUrl) {
        StringBuilder sources = new StringBuilder();
        StringBuilder params = new StringBuilder();
        for (var catalogue : catalogues) {
            for (var table : catalogue.getTables()) {
                sources.append(URLEncoder.encode(table.getName(), StandardCharsets.UTF_8)).append("%20");
                table.getColumns().forEach((k, v) -> params
                        .append(URLEncoder.encode(k, StandardCharsets.UTF_8))
                        .append("=")
                        .append(URLEncoder.encode(v, StandardCharsets.UTF_8))
                        .append("&"));
            }
        }
        params.append("&"); // in case no params were added, otherwise double '&' is workable
        if (radiusType == Radius.ARCMIN) {
            params.append(URLEncoder.encode("-c.rm=", StandardCharsets.UTF_8));
        } else if (radiusType == Radius.DEG) {
            params.append(URLEncoder.encode("-c.rd=", StandardCharsets.UTF_8));
        } else {
            params.append(URLEncoder.encode("-c.rs=", StandardCharsets.UTF_8));
        }
        params.append(URLEncoder.encode(radius, StandardCharsets.UTF_8));
        params.append("&");
        params.append("-out.max=unlimited");

        var uri = URI.create(baseUrl + BASE_PARAM
                + URLEncoder.encode("-source=", StandardCharsets.UTF_8)
                + sources
                + URLEncoder.encode("-c=" + identification, StandardCharsets.UTF_8).replace("+", "%20")
                + "&"
                + params);
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

    /**
     * This function is not in interface, because it is specific only for Vizier.
     * Given the input, create URI that allows for further inspection of specified catalogue.
     * According to the ASU qualifications for VizieR, it is required to decode arguments,
     * more on https://cdsarc.u-strasbg.fr/doc/asu-summary.htx
     * @param catalogueName non-decoded input from user
     * @param baseUrl path to queried server
     * @return URI object
     */
    public URI createMetaDataRequest(String catalogueName, String baseUrl) {
        return URI.create(baseUrl + BASE_PARAM
                + URLEncoder.encode("-source=" + catalogueName, StandardCharsets.UTF_8)
                + "&"
                + URLEncoder.encode("-meta.all", StandardCharsets.UTF_8)
                + "&"
                + URLEncoder.encode("-meta.max=50", StandardCharsets.UTF_8));
    }
}
