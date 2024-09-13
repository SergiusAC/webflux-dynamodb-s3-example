package com.example.awssoundservice.controller;

import com.example.awssoundservice.response.GeneralResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/echo")
public class EchoController {

    @GetMapping("/ping-pong")
    public Mono<GeneralResponse<String>> pingPong(@RequestParam String message) {
        return Mono.just(GeneralResponse.successResponse(message));
    }

}
