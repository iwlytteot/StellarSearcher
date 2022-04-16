package controller.task;

import controller.http.Request;
import lombok.extern.slf4j.Slf4j;
import model.Catalogue;
import model.Radius;
import model.exception.TimeoutQueryException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Class that wraps searching in servers, which is consisted of several parts.
 */
@Component
@Slf4j
public class Searcher {

    /**
     * Asynchronous method that proceeds to search throughout all catalogues and inputs thereby.
     * @param service instance of service that implements Request interface
     * @param catalogues catalogues to be searched
     * @param input user input
     * @param radius user radius input
     * @param type type of radius
     * @param server path to queried server
     * @param timeout if timeout is needed to be set
     * @return CompletableFuture of type List<String> or CompletableFuture of type TimeoutQueryException.
     * Former is returned if search was successful, latter when timeout during querying.
     */
    @Async
    public CompletableFuture<List<String>> start(Request service, List<Catalogue> catalogues, String input,
                                                 String radius, Radius type, String server, boolean timeout) {
        List<String> output = new ArrayList<>();
        List<CompletableFuture<String>> tasks = new ArrayList<>();

        var requests = service.createDataRequest(catalogues, input, radius, type, server);

        //Start all asynchronous methods
        for (var request : requests) {
            tasks.add(service.sendRequest(request, timeout));
        }

        //Retrieve all results
        for (var task : tasks) {
            try {
                output.add(task.get());
            } catch (InterruptedException | ExecutionException e) {
                log.error(e.getCause().toString());
                if (e.getCause() instanceof TimeoutQueryException) {
                    return CompletableFuture.failedFuture(new TimeoutQueryException());
                }
            }
        }
        return CompletableFuture.completedFuture(output);
    }
}
