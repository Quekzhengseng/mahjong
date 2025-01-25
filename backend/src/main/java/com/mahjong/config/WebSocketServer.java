package com.mahjong.config;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class WebSocketServer {
    private final Server server;
    private final WebSocketHandler wsHandler;

    public WebSocketServer() {
        // Create the server
        server = new Server();
        
        // Configure server connector
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(80);
        server.addConnector(connector);
        
        // Create WebSocket handler
        wsHandler = new WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory factory) {
                factory.register(GameWebSocketHandler.class);
                factory.getPolicy().setIdleTimeout(10000);
            }
        };
        
        // Wrap WebSocket handler to be compatible with Server.setHandler()
        HandlerWrapper handlerWrapper = new HandlerWrapper() {
            @Override
            protected void doStart() throws Exception {
                super.doStart();
                setHandler(wsHandler);
            }
        };
        
        // Set the handler wrapper on the server
        server.setHandler(handlerWrapper);
    }

    @PostConstruct
    public void start() throws Exception {
        server.start();
    }

    @PreDestroy
    public void stop() throws Exception {
        server.stop();
    }
}