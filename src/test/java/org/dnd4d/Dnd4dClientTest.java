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
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(VertxUnitRunner.class)
public class Dnd4dClientTest {

    private Vertx vertx;

    private Dnd4dClient sut;

    private HttpServer testServer;

    @Mock
    private LedController ledController;

    @Before
    public void setUp() {
        initMocks(this);

        sut = new Dnd4dClient("localhost", 8081, "/test", ledController);
    }

    @After
    public void tearDown() {
        testServer.close();
    }

    private HttpServer createTestServer(int port, List<String> messages, Optional<Async> async) {
        return Vertx.vertx().createHttpServer()
                .websocketHandler(websocket -> {
                    messages.stream().forEach(websocket::writeFinalTextFrame);
                    websocket.close();
                    async.ifPresent(Async::complete);;
                })
                .listen(port);

    }

    @Test(timeout = 1000)
    public void start_clientConnectsToServer(TestContext context) {
        Async async = context.async();

        testServer = createTestServer(8081, emptyList(), ofNullable(async));

        vertx = Vertx.vertx();
        vertx.deployVerticle(sut,
                context.asyncAssertSuccess());
    }

    @Test(timeout = 1000)
    public void start_clientTurnsOnLed(TestContext context) {
        Async async = context.async();

        testServer = createTestServer(8081, asList("ON"), ofNullable(async));

        vertx = Vertx.vertx();
        vertx.deployVerticle(sut,
                context.asyncAssertSuccess(result -> {
                    verify(ledController).on();
                    verify(ledController, never()).off();
                }));
    }

    @Test(timeout = 1000)
    public void start_clientTurnsOffLed(TestContext context) {
        Async async = context.async();

        testServer = createTestServer(8081, asList("OFF"), ofNullable(async));

        vertx = Vertx.vertx();
        vertx.deployVerticle(sut,
                context.asyncAssertSuccess(result -> {
                    verify(ledController).off();
                    verify(ledController, never()).on();
                }));
    }

    @Test(timeout = 1000)
    public void start_clientTurnsLedOnAndOffMutipleTimes(TestContext context) {
        Async async = context.async();

        testServer = createTestServer(8081, asList("ON", "OFF", "ON", "OFF"), ofNullable(async));

        vertx = Vertx.vertx();
        vertx.deployVerticle(sut,
                context.asyncAssertSuccess(result -> {
                    verify(ledController, times(2)).on();
                    verify(ledController, times(2)).off();
                }));
    }

    @Test(timeout = 1000)
    public void start_clientIgnoresUnknownMessages(TestContext context) {
        Async async = context.async();

        testServer = createTestServer(8081, asList("Hallo"), ofNullable(async));

        vertx = Vertx.vertx();
        vertx.deployVerticle(sut,
                context.asyncAssertSuccess(result -> {
                    verify(ledController, never()).on();
                    verify(ledController, never()).off();
                }));
    }}