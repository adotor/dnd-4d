package org.dnd4d;

import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.WebSocketFrame;

public class Dnd4dClient extends AbstractVerticle {

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
        vertx
                .createHttpClient(new HttpClientOptions().setDefaultPort(8081))
                .websocket("/myuri", websocket -> {
                    websocket.frameHandler(frame -> {
                        System.out.println("Client: Received message:" + frame.textData());

                        if (!canHandle(frame)) {
                            System.out.println("Ignoring unknown event.");
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
