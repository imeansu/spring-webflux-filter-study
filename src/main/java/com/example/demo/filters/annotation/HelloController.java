package com.example.demo.filters.annotation;

import com.example.demo.filters.common.ResponseDto;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class HelloController {

    @RequestMapping("annotation/hello")
    public Mono<ResponseDto> hello() {
        if (true) throw new RuntimeException("An error occurred");
        return Mono.just(new ResponseDto("Hello, World!"));
    }
}
