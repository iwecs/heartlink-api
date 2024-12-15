package com.ss.heartlinkapi.config.batch;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BatchConfig {

    @Bean(name = "batchRestTemplate")
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
