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

@Component
@Slf4j
public class Searcher {

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
