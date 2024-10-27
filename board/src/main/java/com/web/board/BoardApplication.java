package com.web.board;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
// import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
@SpringBootApplication
@EnableCaching
@EnableElasticsearchRepositories(basePackages = "com.web.board.repository")
public class BoardApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoardApplication.class, args);
    }
}
    