package ch.trivadis.com.verticle;


import ch.trivadis.com.sensors.GrovePi;
import ch.trivadis.com.sensors.Ultrasonic;
import ch.trivadis.com.util.WebSocketRepository;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 20.04.15.
 */
public class UltrasonicWebSocketSensor extends AbstractVerticle {
    private String url = "";
    private int port = 0;
    private int scheduleTime = 0;
    private boolean testmode = false;
    private final String URL_DEFAULT = "/ultrasonic";
    private final String PORT_DEFAULT = "8080";
    private final String SCHEDULE_TIME = "1000";
    private final boolean TESTMODE = false;
    private final WebSocketRepository repository = new WebSocketRepository();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private Ultrasonic ultrasonic = null;


    @Override
    public void start(Future<Void> startFuture) throws Exception {
        initConfiguration(config());

        vertx.createHttpServer().websocketHandler(ws -> registerWSPath(ws)).listen(port);

        initSensor();

        initScheduler();

        startFuture.complete();
    }

    private void registerWSPath(ServerWebSocket ws) {
        if (ws.path().toLowerCase().equals(url)) {
            ws.handler((event) -> wsDistanceHandler(ws));
            ws.endHandler((event) -> repository.removeWebSocket(ws));
            ws.closeHandler((event) -> repository.removeWebSocket(ws));
            repository.addWebSocket(ws);
        }
    }

    private void initScheduler() {
        vertx.setPeriodic(scheduleTime, (eventTime) ->
                        readAsync((distance) ->
                                repository.getWebSockets().
                                        forEach(ws ->
                                                ws.writeMessage(Buffer.buffer(distance))))


        );
    }

    private void initSensor() {
        try {
            if (!testmode)
                ultrasonic = new Ultrasonic(
                        new GrovePi().createI2cPin(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void wsDistanceHandler(final ServerWebSocket ws) {
        readAsync((distance) -> ws.writeMessage(Buffer.buffer(distance)));

    }

    private void readAsync(Consumer<String> consumer) {
        CompletableFuture.
                supplyAsync(() -> readFromUltrasonicSensor(), executor).
                thenAccept((distance) -> consumer.accept(distance));

    }


    private String readFromUltrasonicSensor() {
        try {
            return ultrasonic != null ? Integer.valueOf(ultrasonic.getDistance()).toString() : "0";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "0";
    }

    /**
     * init configuration from config file or take default values
     *
     * @param config
     */
    private void initConfiguration(JsonObject config) {
        url = config.getString("wsPath", URL_DEFAULT);
        port = Integer.valueOf(config.getString("port", PORT_DEFAULT));
        testmode = config.getBoolean("testmode", TESTMODE);
        scheduleTime = Integer.valueOf(config.getString("scheduleTime", SCHEDULE_TIME));
    }

    /**
     * for testing purposes
     * @param args
     */
    public static void main(String[] args) {
        DeploymentOptions options = new DeploymentOptions().setInstances(1);
        options.setConfig(new JsonObject().put("testmode", true).put("scheduleTime", "500"));
        Vertx.vertx().deployVerticle(new UltrasonicWebSocketSensor(), options);
    }


}
