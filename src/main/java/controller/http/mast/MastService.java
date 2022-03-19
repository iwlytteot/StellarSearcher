package controller.http.mast;

import controller.http.Request;
import model.Catalogue;
import model.Radius;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for MAST catalogue. For official API, check https://archive.stsci.edu/vo/mast_services.html.
 */
@Component
public class MastService implements Request {
    private static final String BASE_URL = "https://archive.stsci.edu/";
    private static final String BASE_PARAMS = "search.php?action=Search&outputformat=VOTable&";

    public List<URI> createDataRequest(List<Catalogue> catalogues, String identification, String radius, Radius radiusType) {
        var output = new ArrayList<URI>();
        StringBuilder base = new StringBuilder();

        base.append("&radius=");
        base.append(URLEncoder.encode(radius, StandardCharsets.UTF_8));
        base.append("&target=");
        base.append(URLEncoder.encode(identification, StandardCharsets.UTF_8));

        for (var table : catalogues.get(0).getTables()) {
            String source = table.getName() + "/";
            StringBuilder params = new StringBuilder();
            table.getColumns().forEach((k, v) -> params
                    .append(URLEncoder.encode(k, StandardCharsets.UTF_8))
                    .append("=")
                    .append(URLEncoder.encode(v, StandardCharsets.UTF_8))
                    .append("&"));
            output.add(URI.create(BASE_URL
                + source
                + BASE_PARAMS
                + base
                + "&"
                + params));
        }
        return output;
    }
}
