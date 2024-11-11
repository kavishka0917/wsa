import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.logging.Level;
import java.util.logging.Logger;

@ServerEndpoint("/ws/dht")
public class SensorDataSocket {

    private static final Logger LOGGER = Logger.getLogger(SensorDataSocket.class.getName());
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        LOGGER.log(Level.INFO, "Connected: {0}", session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        LOGGER.log(Level.INFO, "Received data from ESP32: {0}", message);

        // Broadcast the received data to all connected clients
        synchronized (sessions) {
            for (Session clientSession : sessions) {
                if (clientSession.isOpen()) {
                    try {
                        clientSession.getBasicRemote().sendText(message);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Error sending message to client", e);
                    }
                }
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        LOGGER.log(Level.INFO, "Disconnected: {0}", session.getId());
    }
}
