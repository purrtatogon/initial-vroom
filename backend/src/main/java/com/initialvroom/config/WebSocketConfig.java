package com.initialvroom.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration using STOMP over SockJS.
 * First time using WebSockets in Spring Boot.
 */
@Configuration
@EnableWebSocketMessageBroker  // Enables STOMP over WebSocket messaging
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // In-memory broker for destinations prefixed with /topic (e.g. /topic/race)
        registry.enableSimpleBroker("/topic");
        // Client messages to /app/* are routed to @MessageMapping methods
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/vroom-ws")
                .setAllowedOriginPatterns("*")  // Allow all origins (needed behind nginx proxy; tighten for prod)
                .withSockJS();  // SockJS fallback for browsers that don't support WebSocket
    }
}
