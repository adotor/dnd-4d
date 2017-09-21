package org.dnd4d;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class Dnd4dClientTest {

    private Vertx vertx;

    private HttpServer testServer;

    @Before
    public void setUp() {
        testServer = createTestServer();
    }

    @After
    public void tearDown() {
        testServer.close();
    }

    private HttpServer createTestServer() {
        return Vertx.vertx().createHttpServer();
    }

    @Test(timeout = 1000)
    public void start_clientConnectsToServer(TestContext context) {
        Async async = context.async();

        testServer.websocketHandler(websocket -> {
            System.out.println("Server: Client connected!");
            websocket.writeFinalTextFrame("Hallo");
            async.complete();
        })
        .listen(8081);

        vertx = Vertx.vertx();
        vertx.deployVerticle(Dnd4dClient.class.getName(),
                context.asyncAssertSuccess());
    }
}