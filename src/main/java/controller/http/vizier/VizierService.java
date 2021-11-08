package controller.http.vizier;

import controller.http.Request;
import model.Catalogue;
import model.CatalogueQueryException;
import model.Radius;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class VizierService implements Request {
    private static final String BASE_URL = "https://vizier.u-strasbg.fr/viz-bin/votable?";

    @Override
    public List<URI> createDataRequest(List<Catalogue> catalogues, String coordinates, String radius, Radius radiusType) {
        StringBuilder sources = new StringBuilder();
        StringBuilder params = new StringBuilder();
        for (var catalogue : catalogues) {
            for (var table : catalogue.getTables()) {
                sources.append(URLEncoder.encode(table.getName(), StandardCharsets.UTF_8)).append("%20");
                table.getColumns().forEach((k, v) -> params
                        .append(URLEncoder.encode(k + "=" + v, StandardCharsets.UTF_8))
                        .append("&"));
            }
        }
        params.append("&"); // in case no params were added, otherwise double '&' is workable
        if (radiusType == Radius.ARCMIN) {
            params.append(URLEncoder.encode("-c.rm=", StandardCharsets.UTF_8));
        }
        else if (radiusType == Radius.DEG) {
            params.append(URLEncoder.encode("-c.rd=", StandardCharsets.UTF_8));
        }
        else {
            params.append(URLEncoder.encode("-c.rs=", StandardCharsets.UTF_8));
        }
        params.append(URLEncoder.encode(radius,StandardCharsets.UTF_8));
        params.append("&");
        params.append("-out.max=unlimited");

        var uri = URI.create(BASE_URL
                + URLEncoder.encode("-source=", StandardCharsets.UTF_8)
                + sources
                + URLEncoder.encode("-c=" + coordinates , StandardCharsets.UTF_8)
                + "&"
                + params);
        var output = new ArrayList<URI>();
        output.add(uri);
        return output;
    }

    @Override
    public void sendRequest(URI uri) throws CatalogueQueryException {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(uri).GET().build();
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            FileWriter myWriter = new FileWriter("data/vizier_data.txt");
            myWriter.write(response.body());
            myWriter.close();

        } catch (IOException | InterruptedException e) {
            throw new CatalogueQueryException(e.getMessage());
        }
    }

    /**
     * This function is not in interface, because it is specific only for Vizier.
     * Given the input, create URI that allows for further inspection of specified catalogue.
     * According to the ASU qualifications for VizieR, it is required to decode arguments,
     * more on https://cdsarc.u-strasbg.fr/doc/asu-summary.htx
     *
     * @param catalogueName non-decoded input from user
     * @return URI for further process
     */
    public URI createMetaDataRequest(String catalogueName) {
        return URI.create(BASE_URL
                + URLEncoder.encode("-source=" + catalogueName, StandardCharsets.UTF_8)
                + "&"
                + URLEncoder.encode("-meta.all", StandardCharsets.UTF_8));
    }
}
