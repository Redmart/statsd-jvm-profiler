package com.etsy.statsd.profiler.server;

import com.etsy.statsd.profiler.Profiler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.impl.RouterImpl;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * Sets up a simple embedded HTTP server for interacting with the profiler while it runs
 *
 * @author Andrew Johnson
 */
public class ProfilerServer {
    private static final Logger LOGGER = Logger.getLogger(ProfilerServer.class.getName());
    private static final Vertx VERTX = Vertx.factory.vertx();

    private ProfilerServer() { }

    /**
     * Start an embedded HTTP server
     *
     * @param activeProfilers The active profilers
     * @param port The port on which to bind the server
     */
    public static void startServer(final Map<String, ScheduledFuture<?>> runningProfilers, final Map<String, Profiler> activeProfilers, final int port, final AtomicReference<Boolean> isRunning, final LinkedList<String> errors) {
        final HttpServer server = VERTX.createHttpServer();
        server.requestHandler(RequestHandler.getMatcher(new RouterImpl(VERTX), runningProfilers, activeProfilers, isRunning, errors));
        server.listen(port, new Handler<AsyncResult<HttpServer>>() {
            @Override
            public void handle(AsyncResult<HttpServer> event) {
                if (event.failed()) {
                    server.close();
                    startServer(runningProfilers, activeProfilers, port + 1, isRunning, errors);
                } else if (event.succeeded()) {
                    LOGGER.info("Profiler server started on port " + port);
                }
            }
        });
    }
}