package controller.http.simbad;

import controller.http.Request;
import model.Catalogue;
import model.Radius;

import java.net.URI;
import java.util.List;

public class SimbadService implements Request {
    @Override
    public List<URI> createDataRequest(List<Catalogue> catalogues, String coordinates, String radius, Radius radiusType) {
        return null;
    }

    @Override
    public void sendRequest(URI uri) {

    }
}
