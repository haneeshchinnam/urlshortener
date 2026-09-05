package com.example.urlshortener.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class IdRepository {

    private final JdbcTemplate jdbcTemplate;

    public IdRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long getNextSequence() {
        return jdbcTemplate.queryForObject(
                "SELECT nextval('url_id_seq')",
                Long.class
        );
    }
}