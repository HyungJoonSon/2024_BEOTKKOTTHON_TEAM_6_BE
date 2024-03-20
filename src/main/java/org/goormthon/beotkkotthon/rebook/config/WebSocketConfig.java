package org.goormthon.beotkkotthon.rebook.config;

import lombok.RequiredArgsConstructor;
import org.goormthon.beotkkotthon.rebook.intercepter.handler.SocketErrorHandler;
import org.goormthon.beotkkotthon.rebook.intercepter.pre.SocketUserIdArgumentResolver;
import org.goormthon.beotkkotthon.rebook.intercepter.pre.SocketUserIdInterceptor;
import org.goormthon.beotkkotthon.rebook.intercepter.pre.HttpUserIdArgumentResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import org.springframework.security.authorization.AuthorizationEventPublisher;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.SpringAuthorizationEventPublisher;
import org.springframework.security.messaging.access.intercept.AuthorizationChannelInterceptor;
import org.springframework.security.messaging.context.AuthenticationPrincipalArgumentResolver;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.*;

import java.util.List;

@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Value("${spring.rabbitmq.host}")
    private String rabbitHost;

    @Value("${spring.rabbitmq.port}")
    private int rabbitPort;

    @Value("${spring.rabbitmq.username}")
    private String rabbitUsername;

    @Value("${spring.rabbitmq.password}")
    private String rabbitPassword;

    private final SocketErrorHandler socketErrorHandler;
    private final SocketUserIdInterceptor socketUserIdInterceptor;
    private final SocketUserIdArgumentResolver socketUserIdArgumentResolver;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config
                .setPathMatcher(new AntPathMatcher("."))
                .setApplicationDestinationPrefixes("/pub")

                .enableStompBrokerRelay("/exchange")
                .setRelayHost(rabbitHost)
                .setRelayPort(rabbitPort)
                .setSystemLogin(rabbitUsername)
                .setSystemPasscode(rabbitPassword)
                .setClientLogin(rabbitUsername)
                .setClientPasscode(rabbitPassword)
                .setVirtualHost("/");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
                .setAllowedOrigins("*");

        registry.setErrorHandler(socketErrorHandler);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(socketUserIdArgumentResolver);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(socketUserIdInterceptor);
    }
}
