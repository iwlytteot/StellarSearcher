package controller.http;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import model.Catalogue;
import model.Radius;
import model.exception.CatalogueQueryException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * General class for wrapping and calling HTTP GET requests.
 * @param <T> Type of class that is to be used in HTTP GET calls.
 *           Available classes: MastService, SimbadService, VizierService.
 */
@Data
@Slf4j
public class GetDataTask<T extends Request> implements Callable<List<String>> {
    private final List<Catalogue> catalogues;
    private final String input;
    private final String radius;
    private final Radius type;
    private final Class<T> serviceClass;
    private final String server;

    /**
     * Wraps retrieving list of URIs and respective HTTP GET calls.
     * @return List of strings such that a string is literal result from respective server.
     */
    @Override
    public List<String> call() {
        var service = getInstanceOfService(serviceClass);
        var output = new ArrayList<String>();
        var requests = service.createDataRequest(catalogues, input, radius, type, server);
        for (var request : requests) {
            try {
                output.add(service.sendRequest(request));
            }
            catch(CatalogueQueryException ex) {
                log.error("Error during retrieving data: " + request.toString());
            }
        }
        return output;
    }

    /**
     * Retrieves correct instance of Service class that is used in call() method.
     * @param tClass type of Service class
     * @return instance of Service class
     */
    public T getInstanceOfService(Class<T> tClass) {
        try {
            return tClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.error("Error during retrieving instance of " + tClass.getName() + " Service: " + e.getMessage());
        }
        return null;
    }
}
