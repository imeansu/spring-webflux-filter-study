package com.example.demo.filters.functional;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@Component
public class Router {

    @Bean
    public RouterFunction<ServerResponse> routes(HelloHandler helloHandler) {
        return RouterFunctions
                .route(POST("/functional/hello"), helloHandler::hello)
                .filter(new LoggingHandlerFilterFunction())
                .filter(new ExceptionHandlerFilterFunction());
    }

}
