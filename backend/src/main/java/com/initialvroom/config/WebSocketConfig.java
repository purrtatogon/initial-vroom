package com.initialvroom.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP broker + SockJS endpoint — topic pub/sub without a custom wire protocol (learned from Spring docs / guides).
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Simple in-memory broker — good enough for this project, no need for RabbitMQ or ActiveMQ
        // Any destination starting with /topic will be handled by this broker
        registry.enableSimpleBroker("/topic");

        // /app prefix is for messages FROM the client (we don't use this yet, but STOMP needs it)
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // /vroom-ws is where the frontend connects to open the WebSocket
        registry.addEndpoint("/vroom-ws")
                .setAllowedOriginPatterns("*")  // allows cross-origin connections from any frontend URL
                .withSockJS();  // fallback for browsers/proxies that block raw WebSocket
    }
}
