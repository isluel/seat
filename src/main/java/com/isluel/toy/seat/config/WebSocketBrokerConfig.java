package com.isluel.toy.seat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * @author KYC. Infotrol Technology.
 * @version 1.0
 * DATE: 25. 2. 12.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // client 에서 websocket 연결시 사용할 API 경로 설정
        registry.addEndpoint("/reserve/waiting", "/reserve/seat")
                .setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 해당 주소를 구독하고 있는 Client 에게 메시지 전송
        registry.enableSimpleBroker("/sub");
        // 클라이언트가 메시지 보낼 때 관련 경로 설정
        registry.setApplicationDestinationPrefixes("/pub");
        // 사용자별 목적지 접두사
//        registry.setUserDestinationPrefix("/user");
    }
}
