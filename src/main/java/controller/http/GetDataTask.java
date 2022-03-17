package controller.http;

import controller.http.vizier.VizierService;
import model.Catalogue;
import model.Radius;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class GetDataTask implements Callable<List<String>> {
    private final List<Catalogue> catalogues;
    private final String input;
    private final String radius;
    private final Radius type;

    public GetDataTask(List<Catalogue> catalogues, String input, String radius, Radius type) {
        this.catalogues = catalogues;
        this.input = input;
        this.radius = radius;
        this.type = type;
    }

    @Override
    public List<String> call() throws Exception {
        if (catalogues.isEmpty()) {
            return null;
        }
        var service = new VizierService();

        var output = new ArrayList<String>();
        var requests = service.createDataRequest(catalogues, input, radius, type);
        for (var request : requests) {
            output.add(service.sendRequest(request));
        }
        return output;
    }
}
