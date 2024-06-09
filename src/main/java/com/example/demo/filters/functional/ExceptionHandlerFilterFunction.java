package com.example.demo.filters.functional;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class ExceptionHandlerFilterFunction implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction next) {
        Mono<ServerResponse> response;
        try {
            response = next.handle(request);
        }
        catch (Throwable ex) {
            response = Mono.error(ex);
        }

        return response.onErrorResume(ex -> {
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue("Error: " + ex.getMessage());
        });
    }
}
