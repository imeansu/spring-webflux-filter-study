package com.example.demo.filters.functional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

public class LoggingHandlerFilterFunction implements HandlerFilterFunction<ServerResponse, ServerResponse> {
    private static final Logger log = LoggerFactory.getLogger(LoggingHandlerFilterFunction.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
        return request.bodyToMono(String.class).singleOptional()
                .flatMap(requestBody -> {
                    // request body 를 bodyToMono 로 읽었으므로 새로운 request 를 만들어야 함
                    ServerRequest.Builder requestBuilder = ServerRequest.from(request);
                    String body = requestBody.orElse(null);
                    if (body != null) {
                        requestBuilder.body(body);
                    }

                    ServerRequest newRequest = requestBuilder.build();

                    return next.handle(newRequest)
                            .flatMap(response -> {
                                log(body, response);
                                return Mono.just(response);
                            }).doOnError(e -> log(body, e));
                });
    }

    private void log(String requestBody, Throwable e) {
        log.error("Request body: {}", requestBody, e);
    }

    private void log(String requestBody, ServerResponse response) {
        String responseBody = null;

        if (response instanceof EntityResponse) {
            try {
                responseBody = mapper.writeValueAsString(((EntityResponse<?>) response).entity());
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize response body", e);
            }
        }

        log.info("Request body: {}, Response Body: {}", requestBody, responseBody);
    }
}
