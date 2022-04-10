package controller.http;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import model.exception.ResolverQueryException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

/**
 * A class that is used for converting from named identification to coordinates. These coordinates are
 * in sexagesimal format. Furthermore, this class is a task that returns searched coordinates.
 */
@Data
@Slf4j
public class SesameResolver implements Callable<String> {
    private static final String BASE_URL = "https://cdsweb.u-strasbg.fr/cgi-bin/nph-sesame/-oxp/SNV?";
    private final String input;

    @Override
    public String call() throws ResolverQueryException {
        return getPosition(request(input));
    }

    /**
     * Creates and sends HTTP GET request to a server where Sesame resolver resides.
     * @param input searched object, can be either named identification or coordinates
     * @return resolved coordinates
     */
    private String request(String input) {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(URI.create(BASE_URL + URLEncoder.encode(input, StandardCharsets.UTF_8))).GET().build();
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (InterruptedException | IOException e) {
            log.error("Error while sending or retrieving data from Sesame resolver: " + e.getMessage());
        }
        log.warn("For input: " + input + ", there was no result.");
        return "";
    }

    /**
     * Method that retrieves resolved coordinates from XML file. In this case, method retrieves String,
     * which is an XML string that was obtained from Sesame resolver.
     * @param input XML object as String
     * @return resolved coordinates
     */
    private String getPosition(String input) throws ResolverQueryException{
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new ByteArrayInputStream(input.getBytes()));
            doc.getDocumentElement().normalize();
            var el = doc.getElementsByTagName("jpos");
            if (el.getLength() == 0) {
                log.warn("No result while parsing XML file: " + input);
                throw new ResolverQueryException();
            }
            return el.item(0).getFirstChild().getNodeValue();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.error("Exception while parsing XML file: " + input + "\n\n" + e.getMessage());
            throw new ResolverQueryException();
        }
    }
}