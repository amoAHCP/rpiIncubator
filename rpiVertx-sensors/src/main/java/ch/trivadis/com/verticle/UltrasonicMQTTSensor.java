package ch.trivadis.com.verticle;


import ch.trivadis.com.sensors.GrovePi;
import ch.trivadis.com.sensors.Ultrasonic;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 20.04.15.
 */
public class UltrasonicMQTTSensor extends AbstractVerticle {
    private int scheduleTime = 0;
    private boolean testmode = false;
    private final String SCHEDULE_TIME = "1000";
    private final boolean TESTMODE = false;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private Ultrasonic ultrasonic = null;
    private MqttClient client;
    private MqttTopic topic;
    private String mqttProtocol;
    private final String MQTT_PROTOCOL = "tcp://";
    private String mqttIp;
    private final String MQTT_IP = "test.mosquitto.org";
    private String mqttPort;
    private final String MQTT_PORT = "1883";


    @Override
    public void start(Future<Void> startFuture) throws Exception {
        initConfiguration(config());

        connect();

        initSensor();

        initScheduler();

        startFuture.complete();
    }


    private void connect() {
        try {
            client = new MqttClient(mqttProtocol + mqttIp + ":" + mqttPort, MqttClient.generateClientId(), new MemoryPersistence());
            client.connect();
            topic = client.getTopic("ultrasonicSensor");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    private void initScheduler() {
        vertx.setPeriodic(scheduleTime, (eventTime) ->
                readAsync(this::publishDistanceToTopic));
    }

    private void publishDistanceToTopic(String distance) {
        try {
            topic.publish(new MqttMessage(payLoadTemplate(distance).getBytes()));
            if (testmode) System.out.println("send: " + payLoadTemplate(distance));
        } catch (MqttException e) {
            e.printStackTrace();
        }
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
        testmode = config.getBoolean("testmode", TESTMODE);
        scheduleTime = Integer.valueOf(config.getString("scheduleTime", SCHEDULE_TIME));
        mqttProtocol = config.getString("mqttProtocol", MQTT_PROTOCOL);
        mqttIp = config.getString("mqttIp", MQTT_IP);
        mqttPort = config.getString("mqttPort", MQTT_PORT);

    }

    private String payLoadTemplate(final String distance) {
        String val = "{" +
                "\"source\":" + "\"" + getHostName() + "\"," +
                "\"time\":" + "\"" + LocalDate.now() + " " + LocalTime.now() + "\"," +
                "\"distance\":" + "\"" + distance + "\"" +
                "}";

        return val;
    }

    private String getHostName() {
        try {
            String result = InetAddress.getLocalHost().getHostName();
            if (result != null && !result.isEmpty())
                return result;
        } catch (UnknownHostException e) {
            // failed;  try alternate means.
        }
        String host = System.getenv("COMPUTERNAME");
        if (host != null)
            return host;
        host = System.getenv("HOSTNAME");
        if (host != null)
            return host;
        return null;
    }

    /**
     * for testing purposes
     * @param args
     */
    public static void main(String[] args) {
        DeploymentOptions options = new DeploymentOptions().setInstances(1);
        options.setConfig(new JsonObject().put("testmode", true).put("scheduleTime", "500").put("mqttIp", "test.mosquitto.org"));
        Vertx.vertx().deployVerticle(new UltrasonicMQTTSensor(), options);
    }

}
