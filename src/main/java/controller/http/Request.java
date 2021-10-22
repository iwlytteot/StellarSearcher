package controller.http;

import model.Catalogue;
import model.Radius;

import java.net.URI;
import java.util.List;

public interface Request {
    URI createDataRequest(List<Catalogue> catalogues, String radius, Radius radiusType);
    void sendRequest(URI uri);
}
