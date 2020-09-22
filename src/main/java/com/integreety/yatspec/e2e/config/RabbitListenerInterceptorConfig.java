package com.integreety.yatspec.e2e.config;

import com.integreety.yatspec.e2e.captor.rabbit.ConsumeCaptor;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/*
    This config adds the interception of messages to RabbitMq listeners
*/
@ConditionalOnProperty(name = {"yatspec.lsd.db.connectionstring"})
@ConditionalOnBean(SimpleRabbitListenerContainerFactory.class)
@Configuration
@RequiredArgsConstructor
public class RabbitListenerInterceptorConfig {

    private final SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory;
    private final ConsumeCaptor consumeCaptor;

    @PostConstruct
    public void postConstruct() {
        simpleRabbitListenerContainerFactory.setAfterReceivePostProcessors(this::postProcessMessage);
    }

    private Message postProcessMessage(final Message message) {
        consumeCaptor.captureConsumeInteraction(MessageBuilder.fromMessage(message).build());
        return message;
    }
}