package cz.muni.fi.tovarys.controller.http;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import cz.muni.fi.tovarys.model.Coordinates;
import cz.muni.fi.tovarys.model.exception.ResolverQueryException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
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
import java.util.concurrent.CompletableFuture;

/**
 * A class that is used for converting from named identification to coordinates. These coordinates are
 * in decimal degree format.
 */
@Component
@Data
@Slf4j
public class SesameResolver {
    private static final String BASE_URL = "https://cdsweb.u-strasbg.fr/cgi-bin/nph-sesame/-oxp/SNV?";

    /**
     * Asynchronous method that starts the query.
     * @param input user input, typically named identifier or coordinate
     * @return CompletableFuture either of type Coordinates or of type ResolverQueryException if failed.
     */
    @Async
    public CompletableFuture<Coordinates> start(String input) {
        try {
            return CompletableFuture.completedFuture(getPosition(request(input)));
        } catch (ResolverQueryException e) {
            return CompletableFuture.failedFuture(new ResolverQueryException());
        }
    }

    /**
     * Creates and sends HTTP GET request to a server where Sesame resolver resides.
     * @param input searched object, can be either named identifier or coordinates
     * @return server response, typically XML document
     */
    private String request(String input) {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(URI.create(BASE_URL +
                URLEncoder.encode(input, StandardCharsets.UTF_8).replace("+", "%20"))).GET().build();
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
     * @throws ResolverQueryException if input cannot be resolved or there has been an error
     */
    private Coordinates getPosition(String input) throws ResolverQueryException{
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new ByteArrayInputStream(input.getBytes()));
            doc.getDocumentElement().normalize();
            var raDeg = doc.getElementsByTagName("jradeg");
            var decDeg = doc.getElementsByTagName("jdedeg");
            if (raDeg.getLength() == 0 || decDeg.getLength() == 0) {
                log.warn("No result while parsing XML file: " + input);
                throw new ResolverQueryException();
            }

            return new Coordinates(raDeg.item(0).getFirstChild().getNodeValue(), decDeg.item(0).getFirstChild().getNodeValue());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.error("Exception while parsing XML file: " + input + "\n\n" + e.getMessage());
            throw new ResolverQueryException();
        }
    }
}