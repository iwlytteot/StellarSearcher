package controller.http.vizier;

import controller.http.Request;
import model.Catalogue;
import model.CatalogueQueryException;
import model.Radius;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class VizierRequest implements Request {
    private static final String BASE_URL = "https://vizier.u-strasbg.fr/viz-bin/asu-tsv?";

    @Override
    public URI createDataRequest(List<Catalogue> catalogues, String coordinates, String radius, Radius radiusType) {
        StringBuilder sources = new StringBuilder();
        StringBuilder params = new StringBuilder();
        for (var catalogue : catalogues) {
            for (var table : catalogue.getTables()) {
                sources.append(table.getName()).append("&");
                table.getColumns().forEach((k, v) -> params.append(k).append("=").append(v).append("&"));
            }

        }
        if (radiusType == Radius.ARCMIN) {
            params.append("-c.rm=");
        }
        else if (radiusType == Radius.DEG) {
            params.append("-c.rd=");
        }
        else {
            params.append("-c.rs=");
        }
        params.append(radius);

        return URI.create(BASE_URL + "-source=" + sources + "-c=" + coordinates + "&" + params);
    }

    @Override
    public void sendRequest(URI uri) throws CatalogueQueryException {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(uri).GET().build();
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            FileWriter myWriter = new FileWriter("data.txt");
            myWriter.write(response.body());
            myWriter.close();

        } catch (IOException | InterruptedException e) {
            throw new CatalogueQueryException(e.getMessage());
        }
    }

    /**
     * This function is not in interface, because it is specific only for Vizier.
     * Given the input, create URI that allows for further inspection of specified catalogue.
     * @param catalogueName Input from user
     * @return URI for further process
     */
    public URI createMetaDataRequest(String catalogueName) {
        return URI.create(BASE_URL + "-source=" + catalogueName + "&-meta.all");
    }
}
