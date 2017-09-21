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

    public static final int DEFAULT_PORT = 8081;
    public static final String WEBSOCKET_URI = "/ourWebsocket";

    private enum State {
        ON,OFF
    }

    private LedController ledController;

    public Dnd4dClient() {
        this.ledController = new LedController(RaspiPin.GPIO_01, PinState.LOW);
    }

    public Dnd4dClient(LedController ledController) {
        this.ledController = ledController;
    }

    @Override
    public void start(Future<Void> fut) {
        logger.info("Opening websocket on " + WEBSOCKET_URI + ":" + DEFAULT_PORT);

        vertx
                .createHttpClient(new HttpClientOptions().setDefaultPort(DEFAULT_PORT))
                .websocket(WEBSOCKET_URI, websocket -> {
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
