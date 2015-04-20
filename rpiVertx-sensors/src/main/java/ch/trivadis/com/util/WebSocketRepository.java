package ch.trivadis.com.util;

import io.vertx.core.http.ServerWebSocket;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A WebSocket repositry to store all connected sockets for each verticle instance. Note, each instance has it's own classloder. Synchronize your communication between socket and verticle instances.
 * Created by Andy Moncsek on 20.04.15.
 */
public class WebSocketRepository {

    private List<ServerWebSocket> webSockets = new CopyOnWriteArrayList<>();

    /**
     * Add a server socket
     *
     * @param webSocket
     */
    public void addWebSocket(ServerWebSocket webSocket) {
        webSockets.add(webSocket);
    }

    /**
     * Get all WebSockets for verticle instance
     *
     * @return
     */
    public List<ServerWebSocket> getWebSockets() {
        return Collections.unmodifiableList(webSockets);
    }

    /**
     * remove a server socket
     *
     * @param webSocket
     */
    public void removeWebSocket(ServerWebSocket webSocket) {
        webSockets.remove(webSocket);
    }
}
