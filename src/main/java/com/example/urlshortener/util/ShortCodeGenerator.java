package com.example.urlshortener.util;

import com.example.urlshortener.repository.IdRepository;
import org.springframework.stereotype.Service;

@Service
public class ShortCodeGenerator {

    private final IdRepository idRepository;

    private static final long EPOCH = 1577836800000L;
    private static final int SEQUENCE_BITS = 22;

    public ShortCodeGenerator(IdRepository idRepository) {
        this.idRepository = idRepository;
    }

    public String generate() {

        long timestamp =
                System.currentTimeMillis() - EPOCH;

        long sequence =
                idRepository.getNextSequence();

        long id =
                (timestamp << SEQUENCE_BITS)
                        | sequence;

        return Base62Encoder.encode(id);
    }
}