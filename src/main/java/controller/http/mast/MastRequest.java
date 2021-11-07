package controller.http.mast;

import java.net.URI;

public class MastRequest implements Runnable {

    private final URI request;
    private final MastService mastService = new MastService();

    public MastRequest(URI request) {
        this.request = request;
    }

    @Override
    public void run() {
        mastService.sendRequest(request);
    }
}

