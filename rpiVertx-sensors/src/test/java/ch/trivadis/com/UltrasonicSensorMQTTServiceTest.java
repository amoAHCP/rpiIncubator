package ch.trivadis.com;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * This Test works only with a valid MQTT server!
 * Created by Andy Moncsek on 20.04.15.
 */
public class UltrasonicSensorMQTTServiceTest extends VertxTestBase {
    private MqttClient client;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        CountDownLatch latch = new CountDownLatch(1);
        DeploymentOptions options = new DeploymentOptions().setInstances(1);
        options.setConfig(new JsonObject().put("testmode", true).put("scheduleTime", "500").put("mqttIp", "test.mosquitto.org"));
        vertx.deployVerticle("ch.trivadis.com.verticle.UltrasonicMQTTSensor", options, asyncResult -> {
            // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
            assertTrue(asyncResult.succeeded());
            // If deployed correctly then start the tests!
            latch.countDown();

        });

        awaitLatch(latch);
        connect();
    }

    private void connect() {
        try {
            client = new MqttClient("tcp://test.mosquitto.org:1883", MqttClient.generateClientId(), new MemoryPersistence());
            client.connect();
            client.subscribe("ultrasonicSensor", 0);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void simpleConnectionTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(5);
        client.setCallback(new MyMQQTClient(latch));

        awaitLatch(latch);
        testComplete();
    }

    public class MyMQQTClient implements MqttCallback {
        private final CountDownLatch latch;

        public MyMQQTClient(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void connectionLost(Throwable cause) {

        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            System.out.println(new String(message.getPayload()));
            if (latch != null) latch.countDown();
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }
    }


}
