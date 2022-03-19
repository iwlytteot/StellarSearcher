package controller.http;

import lombok.Data;
import model.Catalogue;
import model.Radius;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Data
public class GetDataTask<T extends Request> implements Callable<List<String>> {
    private final List<Catalogue> catalogues;
    private final String input;
    private final String radius;
    private final Radius type;
    private final Class<T> serviceClass;

    @Override
    public List<String> call() throws Exception {
        var service = getInstanceOfService(serviceClass);

        var output = new ArrayList<String>();
        var requests = service.createDataRequest(catalogues, input, radius, type);
        for (var request : requests) {
            output.add(service.sendRequest(request));
        }
        return output;
    }

    public T getInstanceOfService(Class<T> tClass) {
        try {
            return tClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
