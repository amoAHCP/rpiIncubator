package ch.trivadis.com.verticle;


import ch.trivadis.com.util.WebSocketRepository;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 20.04.15.
 */
public class TemperatureSensor extends AbstractVerticle {
    private String url = "";
    private final String URL_DEFAULT = "/temperature";
    private final WebSocketRepository repository = new WebSocketRepository();
    private final ExecutorService executor = Executors.newCachedThreadPool();


    @Override
    public void start(Future<Void> startFuture) throws Exception {
        initConfiguration(config());

        vertx.createHttpServer().websocketHandler(ws -> registerWSPath(ws)).listen(8080);

        initScheduler();

        startFuture.complete();
    }

    private void registerWSPath(ServerWebSocket ws) {
        if (ws.path().toLowerCase().equals(url)) {
            ws.handler((event) -> currentTemperatureHandler(ws));
            ws.endHandler((event) -> repository.removeWebSocket(ws));
            ws.closeHandler((event) -> repository.removeWebSocket(ws));
            repository.addWebSocket(ws);

        }
    }

    private void initScheduler() {
        vertx.setPeriodic(5000, (eventTime) ->
                readAsync((temperature) -> repository.getWebSockets().forEach(ws -> ws.writeMessage(Buffer.buffer(temperature))))
        );
    }


    private void currentTemperatureHandler(final ServerWebSocket ws) {
        readAsync((temperature)->ws.writeMessage(Buffer.buffer(temperature)));
    }

    private void readAsync(Consumer<String> consumer) {
        CompletableFuture.
                supplyAsync(() -> readTemperatureFromDevice(), executor).
                thenAccept((distance) -> consumer.accept(distance));

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
