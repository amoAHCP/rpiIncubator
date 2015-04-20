package ch.trivadis.com;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.impl.ws.WebSocketFrameImpl;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Andy Moncsek on 20.04.15.
 */
public class TempSensorServiceTest extends VertxTestBase {
    private HttpClient client;

    @Override
    public void setUp() throws Exception {
        super.setUp();
       CountDownLatch latch = new CountDownLatch(1);

        vertx.deployVerticle("ch.trivadis.com.verticle.TemperatureSensor",asyncResult ->{
            // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
            assertTrue(asyncResult.succeeded());
            // If deployed correctly then start the tests!
            latch.countDown();

        });
        awaitLatch(latch);
        client = vertx.
                createHttpClient(new HttpClientOptions());
    }

    private HttpClient getClient(final Handler<WebSocket> handler, final String path) {

        HttpClient client = vertx.
                createHttpClient(new HttpClientOptions()).websocket(8080, "localhost", path, handler);

        return client;
    }

    @Test
    public void simpleConnectionTest() {
        client.websocket(8080, "localhost", "/temperature", ws -> {
            long startTime = System.currentTimeMillis();
            ws.handler((data) -> {
                System.out.println("current temperature is: " + new String(data.getBytes()));
                assertNotNull(data.getString(0, data.length()));
                ws.close();
                long endTime = System.currentTimeMillis();
                System.out.println("Total execution time simpleConnectAndWrite: " + (endTime - startTime) + "ms");
                testComplete();
            });

            ws.writeFrame(new WebSocketFrameImpl("checkTemp"));
        });


        await();
    }
}
