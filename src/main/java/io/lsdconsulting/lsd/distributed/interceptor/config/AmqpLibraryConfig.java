package io.lsdconsulting.lsd.distributed.interceptor.config;

import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.header.Obfuscator;
import io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit.AmqpHeaderRetriever;
import io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit.RabbitCaptor;
import io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit.mapper.ExchangeNameDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever;
import io.lsdconsulting.lsd.distributed.interceptor.persistance.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "lsd.dist.db.connectionString")
@RequiredArgsConstructor
public class AmqpLibraryConfig {

    @Bean
    @ConditionalOnClass(org.springframework.amqp.core.MessageProperties.class)
    public ExchangeNameDeriver exchangeNameDeriver() {
        return new ExchangeNameDeriver();
    }

    @Bean
    @ConditionalOnClass(org.springframework.amqp.core.Message.class)
    public AmqpHeaderRetriever amqpHeaderRetriever(Obfuscator obfuscator) {
        return new AmqpHeaderRetriever(obfuscator);
    }

    @Bean
    @ConditionalOnBean(name = "amqpHeaderRetriever")
    public RabbitCaptor publishCaptor(final QueueService queueService,
                                      final PropertyServiceNameDeriver propertyServiceNameDeriver,
                                      final TraceIdRetriever traceIdRetriever,
                                      final AmqpHeaderRetriever amqpHeaderRetriever,
                                      @Value("${spring.profiles.active:#{''}}") final String profile) {

        return new RabbitCaptor(queueService, propertyServiceNameDeriver, traceIdRetriever, amqpHeaderRetriever, profile);
    }
}
