package com.tiagotibaes.streams.service;


import com.tiagotibaes.streams.dto.ApproveRequestDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.rabbit.stream.producer.RabbitStreamTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublisherService {

    private final RabbitStreamTemplate rabbitStreamTemplate;

    public void publisher(ApproveRequestDTO approve) {
        log.info("Publisher new request: {}", approve);
        rabbitStreamTemplate.convertAndSend(approve);
    }
}
