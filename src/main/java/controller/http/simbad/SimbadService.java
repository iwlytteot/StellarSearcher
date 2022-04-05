package controller.http.simbad;

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
 * Service for Simbad database. For official API, check http://simbad.u-strasbg.fr/guide/sim-url.htx.
 */
@Component
public class SimbadService implements Request {
    private static final String BASE_PARAM = "/simbad/sim-coo?";
    private static final String SUFFIX_URL = "&output.format=VOTable";
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
}
