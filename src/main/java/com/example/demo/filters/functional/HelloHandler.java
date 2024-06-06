package com.example.demo.filters.functional;

import com.example.demo.filters.common.ResponseDto;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class HelloHandler {

    public Mono<ServerResponse> hello(ServerRequest request) {
        return request.bodyToMono(String.class)
                .doOnNext(System.out::println)
                .then(ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new ResponseDto("Hello, World!"))
                );
    }
}
