package org.dnd4d;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClientOptions;

public class Dnd4dClient extends AbstractVerticle {

    @Override
    public void start(Future<Void> fut) {
        vertx
                .createHttpClient(new HttpClientOptions().setDefaultPort(8081))
                .websocket("/myuri", websocket -> {
                    websocket.frameHandler(frame -> {
                        System.out.println("Client: Received message:" + frame.textData());
                        fut.complete();
                    });
                });

    }
}
