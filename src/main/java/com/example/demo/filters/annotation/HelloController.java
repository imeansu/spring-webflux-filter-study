package com.example.demo.filters.annotation;

import com.example.demo.filters.common.ResponseDto;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class HelloController {

    @RequestMapping("annotation/hello")
    public Mono<ResponseDto> hello(@RequestBody Map<String, Object> body) {
        System.out.println(body);
        return Mono.just(new ResponseDto("Hello, World!"));
    }
}
