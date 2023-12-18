package com.tiagotibaes.streams.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.stream.Message;
import com.rabbitmq.stream.MessageHandler;
import com.tiagotibaes.streams.dto.ApproveRequestDTO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;


import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumerService {

    private final ObjectMapper objectMapper;
    private final RabbitAdmin rabbitAdmin;
    private final Queue createPartition;

    @PostConstruct
    void init() {
        rabbitAdmin.declareQueue(createPartition);
    }


    @Retryable(interceptor = "streamRetryOperationsInterceptorFactoryBean")
    @RabbitListener(
            queues = "#{createPartition.name}",
            containerFactory = "streamContainerFactory"
    )
    void onConsumer(Message in, MessageHandler.Context context) throws IOException {
        log.info("Stream partition message offset: {}", context.offset());
        var approve = objectMapper.readValue(in.getBodyAsBinary(), ApproveRequestDTO.class);
        log.info("Consumer message: {}", approve);
    }

}
