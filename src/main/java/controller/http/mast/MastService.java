package controller.http.mast;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MastService implements Request {
    private static final String BASE_URL = "https://archive.stsci.edu/";
    private static final String BASE_PARAMS = "search.php?action=Search&outputformat=VOTable&";

    public List<URI> createDataRequest(List<Catalogue> catalogues, String coordinates, String radius, Radius radiusType) {
        var output = new ArrayList<URI>();
        StringBuilder params = new StringBuilder();
        params.append("&radius=");
        params.append(URLEncoder.encode(radius, StandardCharsets.UTF_8));

        params.append("&target=");
        params.append(URLEncoder.encode(coordinates, StandardCharsets.UTF_8));

        for (var table : catalogues.get(0).getTables()) {
            String source = table.getName() + "/";
            table.getColumns().forEach((k, v) -> params
                    .append(URLEncoder.encode(k, StandardCharsets.UTF_8))
                    .append("=")
                    .append(URLEncoder.encode(v, StandardCharsets.UTF_8))
                    .append("&"));
            output.add(URI.create(BASE_URL
                + source
                + BASE_PARAMS
                + params));
        }
        return output;
    }

    @Override
    public void sendRequest(URI uri) throws CatalogueQueryException {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(uri).GET().build();
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Pattern pattern = Pattern.compile("https://archive\\.stsci\\.edu/(.*)/");
            Matcher matcher = pattern.matcher(uri.toString());
            String name = "mast";
            if (matcher.find() && matcher.groupCount() >= 1) {
                name = matcher.group(1);
                name = name.replace("/", "_");
            }
            FileWriter myWriter = new FileWriter("data/" + name + ".txt");
            myWriter.write(response.body());
            myWriter.close();

        } catch (IOException | InterruptedException e) {
            throw new CatalogueQueryException(e.getMessage());
        }
    }
}
