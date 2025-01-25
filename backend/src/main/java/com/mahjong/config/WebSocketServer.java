package com.mahjong.config;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class WebSocketServer {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
    
    private final Server server;
    private final WebSocketHandler wsHandler;

    public WebSocketServer() {
        try {
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
            
            logger.info("WebSocket server configured successfully");
        } catch (Exception e) {
            logger.error("Error configuring WebSocket server", e);
            throw new RuntimeException("Failed to configure WebSocket server", e);
        }
    }

    @PostConstruct
    public void start() throws Exception {
        try {
            server.start();
            logger.info("WebSocket server started on port 80");
        } catch (Exception e) {
            logger.error("Failed to start WebSocket server", e);
            throw e;
        }
    }

    @PreDestroy
    public void stop() throws Exception {
        try {
            server.stop();
            logger.info("WebSocket server stopped");
        } catch (Exception e) {
            logger.error("Error stopping WebSocket server", e);
            throw e;
        }
    }
}