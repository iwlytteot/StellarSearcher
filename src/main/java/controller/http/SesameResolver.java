package controller.http;

import model.ResolverQueryException;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Component
public class SesameResolver {
    private static final String BASE_URL = "https://cdsweb.u-strasbg.fr/cgi-bin/nph-sesame/-oxp/SNV?";

    public String resolve(String input) {
        request(input);
        return getPosition();
    }

    private void request(String input) {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(URI.create(BASE_URL + URLEncoder.encode(input, StandardCharsets.UTF_8))).GET().build();
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            FileWriter myWriter = new FileWriter("data/resolver.txt");
            myWriter.write(response.body());
            myWriter.close();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private String getPosition() throws ResolverQueryException {
        File inputFile = new File("data/resolver.txt");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
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