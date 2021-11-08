package controller.http.simbad;

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

public class SimbadService implements Request {
    private static final String BASE_URL = "http://simbad.u-strasbg.fr/simbad/sim-basic?";
    private static final String SUFFIX_URL = "&output.format=VOTable&submit=SIMBAD+search";
    @Override
    public List<URI> createDataRequest(List<Catalogue> catalogues, String coordinates, String radius, Radius radiusType) {
        var s = "http://simbad.u-strasbg.fr/simbad/sim-basic?Ident=100.1+50.1&submit=SIMBAD+search";
        StringBuilder params = new StringBuilder();

        params.append("Ident=").append(URLEncoder.encode(coordinates, StandardCharsets.UTF_8));
        params.append("&Radius.unit="); // in case no params were added, otherwise double '&' is workable
        params.append(radiusType.name.replace(" ", ""));
        params.append("&");
        params.append(URLEncoder.encode(radius, StandardCharsets.UTF_8));

        var uri = URI.create(BASE_URL
            + params.toString()
            + SUFFIX_URL);
        var output = new ArrayList<URI>();
        output.add(uri);
        return output;
    }

    @Override
    public void sendRequest(URI uri) {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(uri).GET().build();
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            FileWriter myWriter = new FileWriter("data/simbad_data.txt");
            myWriter.write(response.body());
            myWriter.close();

        } catch (IOException | InterruptedException e) {
            throw new CatalogueQueryException(e.getMessage());
        }
    }
}
