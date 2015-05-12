package ch.trivadis.com;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.impl.ws.WebSocketFrameImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Andy Moncsek on 20.04.15.
 */
public class UltrasonicSensorServiceTest extends VertxTestBase {
    private HttpClient client;

    @Override
    public void setUp() throws Exception {
        super.setUp();
       CountDownLatch latch = new CountDownLatch(1);
        DeploymentOptions options = new DeploymentOptions().setInstances(1);
        options.setConfig(new JsonObject().put("testmode", true).put("scheduleTime","500"));
        vertx.deployVerticle("ch.trivadis.com.verticle.UltrasonicSensor",options,asyncResult ->{
            // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
            assertTrue(asyncResult.succeeded());
            // If deployed correctly then start the tests!
            latch.countDown();

        });
        awaitLatch(latch);
        client = vertx.
                createHttpClient(new HttpClientOptions());
    }


    @Test
    public void simpleConnectionTest() {
        client.websocket(8080, "localhost", "/ultrasonic", ws -> {
            long startTime = System.currentTimeMillis();
            ws.handler((data) -> {
                System.out.println("current distance is: " + new String(data.getBytes()));
                assertNotNull(data.getString(0, data.length()));
                ws.close();
                long endTime = System.currentTimeMillis();
                System.out.println("Total execution time simpleConnectionTest: " + (endTime - startTime) + "ms");
                testComplete();
            });

            ws.writeFrame(new WebSocketFrameImpl("checkTemp"));
        });


        await();
    }

    @Test
    public void schedulerTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(10);
        client.websocket(8080, "localhost", "/ultrasonic", ws -> {
            long startTime = System.currentTimeMillis();
            ws.handler((data) -> {
                System.out.println("current distance is: " + new String(data.getBytes()));
                assertNotNull(data.getString(0, data.length()));

                long endTime = System.currentTimeMillis();
                System.out.println("Total execution time schedulerTest: " + (endTime - startTime) + "ms");
                latch.countDown();


            });

            ws.writeFrame(new WebSocketFrameImpl("checkTemp"));
        });


       latch.await();
    }
}
