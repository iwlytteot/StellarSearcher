package controller.http;

import lombok.Data;
import model.ResolverQueryException;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

@Data
public class SesameResolver implements Callable<String> {
    private static final String BASE_URL = "https://cdsweb.u-strasbg.fr/cgi-bin/nph-sesame/-oxp/SNV?";

    private final String input;

    @Override
    public String call() throws Exception {
        return getPosition(request(input));
    }

    private String request(String input) {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(URI.create(BASE_URL + URLEncoder.encode(input, StandardCharsets.UTF_8))).GET().build();
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        throw new ResolverQueryException("Input was not resolved");
    }

    private String getPosition(String input) throws ResolverQueryException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new ByteArrayInputStream(input.getBytes()));
            doc.getDocumentElement().normalize();
            var el = doc.getElementsByTagName("jpos");
            if (el.getLength() == 0) {
                throw new ResolverQueryException("Resolving was unsuccessful");
            }
            return el.item(0).getFirstChild().getNodeValue();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        throw new ResolverQueryException("Resolving was unsuccessful");
    }
}