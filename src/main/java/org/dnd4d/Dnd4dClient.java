package org.dnd4d;

import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class Dnd4dClient extends AbstractVerticle {
    private final Logger logger = LoggerFactory.getLogger(Dnd4dClient.class);

    private static final String DEFAULT_HOSTNAME = "10.89.0.115";
    public static final int DEFAULT_PORT = 8081;
    public static final String DEFAULT_URI = "/dnd4d";

    private enum State {
        ON,OFF
    }

    private LedController ledController;

    private final String hostname;
    private final int port;
    private final String uri;

    public Dnd4dClient() {
        this.hostname = DEFAULT_HOSTNAME;
        this.port = DEFAULT_PORT;
        this.uri = DEFAULT_URI;

        this.ledController = new LedController(RaspiPin.GPIO_01, PinState.LOW);
    }

    public Dnd4dClient(String hostname, int port, String uri, LedController ledController) {
        this.hostname = hostname;
        this.port = port;
        this.uri = uri;
        this.ledController = ledController;
    }

    @Override
    public void start(Future<Void> fut) {
        logger.info("Opening websocket on " + DEFAULT_URI + ":" + DEFAULT_PORT);

        vertx
                .createHttpClient(new HttpClientOptions()
                        .setDefaultHost(hostname)
                        .setDefaultPort(port))
                .websocket(uri, websocket -> {
                    websocket.frameHandler(frame -> {
                        logger.info("Received message:" + frame.textData());

                        if (!canHandle(frame)) {
                            logger.warn("Ignoring unknown event.");
                            return;
                        }

                        final State state = State.valueOf(frame.textData());
                        switch (state) {
                            case ON:
                                ledController.on();
                                break;
                            case OFF:
                                ledController.off();
                                break;
                        }
                    })
                    .closeHandler(unused -> {
                        fut.complete();
                    });
                });

    }

    private boolean canHandle(WebSocketFrame frame) {
        if (!frame.isText()) {
            return false;
        }

        try {
            State.valueOf(frame.textData());
        } catch (IllegalArgumentException ex) {
            return false;
        }

        return true;
    }
}
