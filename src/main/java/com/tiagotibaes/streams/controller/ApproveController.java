package com.tiagotibaes.streams.controller;


import com.tiagotibaes.streams.dto.ApproveRequestDTO;
import com.tiagotibaes.streams.service.PublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ApproveController {

    @Autowired
    private final PublisherService publisher;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> request(@RequestBody ApproveRequestDTO approve) {
        publisher.publisher(approve);
        return ResponseEntity.noContent().build();
    }
}
