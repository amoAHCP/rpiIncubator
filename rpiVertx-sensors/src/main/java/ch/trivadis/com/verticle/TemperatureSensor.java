package ch.trivadis.com.verticle;


import ch.trivadis.com.util.WebSocketRepository;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Andy Moncsek on 20.04.15.
 */
public class TemperatureSensor extends AbstractVerticle {
    private String url = "temperature";
    private final String URL_DEFAULT = "temperature";
    private final WebSocketRepository repository = new WebSocketRepository();
    private final ExecutorService executor = Executors.newCachedThreadPool();


    @Override
    public void start() throws Exception {
        initConfiguration(config());
        vertx.createHttpServer().websocketHandler(ws -> {
            if (ws.path().toLowerCase().equals(url)) {
                ws.handler((event) -> currentTemperatureHandler(ws, event));
                ws.endHandler((event) -> repository.removeWebSocket(ws));
                ws.closeHandler((event) -> repository.removeWebSocket(ws));
                repository.addWebSocket(ws);

            }


        }).listen(8080);

        vertx.setPeriodic(5000, (eventTime) ->
                        CompletableFuture.
                                supplyAsync(() -> readTemperatureFromDevice(), executor).
                                thenAccept((temperature) -> repository.getWebSockets().forEach(ws -> ws.writeMessage(Buffer.buffer().appendString(temperature))))
        );
    }


    private void currentTemperatureHandler(final ServerWebSocket ws, final Buffer buffer) {
        // ignore (request) buffer data
        CompletableFuture.
                supplyAsync(() -> readTemperatureFromDevice(), executor).
                thenAccept((temperature) -> ws.writeMessage(Buffer.buffer().appendString(temperature)));
    }


    private String readTemperatureFromDevice() {
        // read data from ??whatever ??
        System.out.println("read ");
        return "24";
    }

    /**
     * init configuration from config file or take default values
     *
     * @param config
     */
    private void initConfiguration(JsonObject config) {
        url = config.getString("wsPath", URL_DEFAULT);
    }


}
