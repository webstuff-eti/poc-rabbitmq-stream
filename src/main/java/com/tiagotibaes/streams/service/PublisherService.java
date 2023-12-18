package com.tiagotibaes.streams.service;


import com.tiagotibaes.streams.dto.ApproveRequestDTO;

import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;

import org.springframework.rabbit.stream.producer.RabbitStreamTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublisherService {

    private final RabbitStreamTemplate rabbitStreamTemplate;

    public void publisher(ApproveRequestDTO login) {
        log.info("Publisher new request: {}", login);
        rabbitStreamTemplate.convertAndSend(login);
    }
}
