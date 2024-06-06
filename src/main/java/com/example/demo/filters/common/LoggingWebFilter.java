package com.example.demo.filters.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.Map;

@Component
public class LoggingWebFilter implements WebFilter {
    private static final Logger log = LoggerFactory.getLogger(LoggingWebFilter.class.getName());

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        LoggingRequestDecorator requestDecorator = new LoggingRequestDecorator(exchange.getRequest());
        LoggingResponseDecorator responseDecorator = new LoggingResponseDecorator(exchange.getResponse());

        return chain.filter(new LoggingWebExchange(exchange, requestDecorator, responseDecorator))
                .then(Mono.fromRunnable(() -> {
                    log.info("requestBody: {}, responseBody: {}", requestDecorator.getBodyString(), responseDecorator.getBodyString());
                }))
                .then();
    }

    static class LoggingWebExchange extends ServerWebExchangeDecorator {
        private final LoggingRequestDecorator requestDecorator;
        private final LoggingResponseDecorator responseDecorator;

        LoggingWebExchange(ServerWebExchange delegate, LoggingRequestDecorator requestDecorator, LoggingResponseDecorator responseDecorator) {
            super(delegate);
            this.requestDecorator = requestDecorator;
            this.responseDecorator = responseDecorator;
        }

        @Override
        public ServerHttpRequest getRequest() {
            return requestDecorator;
        }

        @Override
        public ServerHttpResponse getResponse() {
            return responseDecorator;
        }
    }

    static class LoggingRequestDecorator extends ServerHttpRequestDecorator {
        private static final ObjectMapper mapper = new ObjectMapper();
        private String bodyString;

        LoggingRequestDecorator(ServerHttpRequest delegate) {
            super(delegate);
        }

        public String getBodyString() {
            return bodyString;
        }

        @Override
        public Flux<DataBuffer> getBody() {
            return super.getBody().doOnNext( buffer -> {
                    // changes in the returned buffer's position will not be reflected in the of this data buffer
                    try (DataBuffer.ByteBufferIterator bfs = buffer.readableByteBuffers()) {
                        ByteBuffer bf = bfs.next();
                        byte[] bytes = new byte[bf.remaining()];
                        bf.get(bytes);
                        this.bodyString = mapper.readValue(bytes, Map.class).toString();
                    } catch (Exception e) {
                        log.error("Failed to read request body", e);
                    }
            });
        }
    }

    static class LoggingResponseDecorator extends ServerHttpResponseDecorator {
        private String bodyString;

        public LoggingResponseDecorator(ServerHttpResponse delegate) {
            super(delegate);
        }

        public String getBodyString() {
            return bodyString;
        }

        @Override
        public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
            return super.writeWith(Flux.from(body)
                    .map( buffer -> {
                        final byte[] bytes = new byte[buffer.readableByteCount()];
                        DataBufferUtils.release(buffer.read(bytes));
                        this.bodyString = new String(bytes);
                        return getDelegate().bufferFactory().wrap(bytes);
                    }));
        }
    }
}
