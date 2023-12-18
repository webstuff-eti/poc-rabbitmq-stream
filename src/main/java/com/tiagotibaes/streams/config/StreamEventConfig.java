package com.tiagotibaes.streams.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.rabbit.stream.config.StreamRabbitListenerContainerFactory;
import org.springframework.rabbit.stream.listener.StreamListenerContainer;
import org.springframework.rabbit.stream.producer.RabbitStreamTemplate;
import org.springframework.rabbit.stream.retry.StreamRetryOperationsInterceptorFactoryBean;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.unit.DataSize;

import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.OffsetSpecification;
import com.rabbitmq.stream.impl.StreamEnvironmentBuilder;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class StreamEventConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    Environment env(RabbitProperties rabbitProperties) {
        return new StreamEnvironmentBuilder()
                .host(rabbitProperties.getHost())
                .port(rabbitProperties.getStream().getPort())
                .username(rabbitProperties.getUsername())
                .password(rabbitProperties.getPassword())
                .virtualHost(rabbitProperties.getVirtualHost())
                .build();
    }


    //FIXME: Responsável em criar os Beans dentro do Brocker, ou seja, criar as filas
    @Bean
    RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    //FIXME: Responsável em criar um Template e fazer uso dos mesmos dentro dos nossos Listerners
    //FIXME: Template será utilizado no momento da publicação (PRODUCER) das mensagens
    @Bean
    RabbitStreamTemplate rabbitStreamTemplate(Environment env,
                                              Exchange superStream,
                                              Jackson2JsonMessageConverter jackson2JsonMessageConverter) {
        var template = new RabbitStreamTemplate(env, superStream.getName());
        template.setMessageConverter(jackson2JsonMessageConverter);
        //FIXME: Como possui uma estratégia de rotas, será responsável em receber as mensagens e dividí-las entre as partções
        template.setProducerCustomizer(
                (s, builder) -> builder
                        .routing(Object::toString)
                        .producerBuilder()
        );
        return template;
    }

    //FIXME: Template será utilizado no momento da recepção (CONSUMER) das mensagens
    @Bean
    RabbitListenerContainerFactory<StreamListenerContainer> streamContainerFactory(Environment env) {
        var factory = new StreamRabbitListenerContainerFactory(env);
        factory.setNativeListener(true);
        factory.setConsumerCustomizer(
                (id, builder) ->
                        builder.name(applicationName)
                                .offset(OffsetSpecification.first())
//                                .manualTrackingStrategy()
                                .autoTrackingStrategy().messageCountBeforeStorage(5)
        );
        return factory;
    }

    //FIXME: Interceptor
    @Bean
    StreamRetryOperationsInterceptorFactoryBean streamRetryOperationsInterceptorFactoryBean(RetryTemplate configRetryTemplate) {
        var factoryBean = new StreamRetryOperationsInterceptorFactoryBean();
        factoryBean.setRetryOperations(configRetryTemplate);
        return factoryBean;
    }


    //FIXME: Template será utilizado será utilizado caso o consumidor lance alguma exceção.
    @Bean
    RetryTemplate configRetryTemplate() {
        return RetryTemplate.builder()
                .infiniteRetry()
                .exponentialBackoff(
                        TimeUnit.SECONDS.toMillis(10),
                        1.5,
                        TimeUnit.MINUTES.toMillis(5)
                ).build();
    }

    //FIXME: Responsável em criar o Exchange do Brocker
    @Bean
    Exchange createExchange() {
        return ExchangeBuilder
                .directExchange(applicationName)
                .build();
    }

    //FIXME: Responsável em criar a Fila (Partições) dentro do Brocker
    @Bean
    Queue createPartition() {
        return QueueBuilder
                .durable("partition-1") //FIXME: nome da fila
                .stream()
                .withArgument("x-max-age", "7D") //FIXME: tempo de vida (7 dias )
                .withArgument("x-max-length-bytes",
                        DataSize.ofGigabytes(10).toBytes()) //FIXME: tamanho máximo da partição:(10GB).
                .build();
    }


    //FIXME: responsável em realizar o Binding entre a Exchange (método createExchange) e a fila/ partição (método createPartition)
    @Bean
    Binding streamPartitionBind(Exchange createExchange, Queue createPartition) {
        return BindingBuilder
                .bind(createPartition)
                .to(createExchange)
                .with("")
                .noargs();
    }
}
