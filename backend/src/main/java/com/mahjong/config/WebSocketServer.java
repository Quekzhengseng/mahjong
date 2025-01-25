package com.mahjong.config;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

@Component
public class WebSocketServer {
    private final Server server;

    public WebSocketServer() {
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(80);
        server.addConnector(connector);
        
        WebSocketHandler wsHandler = new WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory factory) {
                factory.register(GameWebSocketHandler.class);
            }
        };
        
        server.setHandler(wsHandler);
    }

    @PostConstruct
    public void start() throws Exception {
        server.start();
    }
}