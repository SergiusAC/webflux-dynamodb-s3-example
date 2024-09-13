package com.example.awssoundservice.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

@Slf4j
public class FileUtils {

    public static Mono<ByteBuffer> dataBuffersToByteBuffer(Flux<DataBuffer> buffers) {
        return DataBufferUtils.join(buffers).map(dataBuffer -> {
            ByteBuffer buffer = ByteBuffer.allocate(dataBuffer.readableByteCount());
            dataBuffer.toByteBuffer(buffer);
            return buffer;
        });
    }

}
