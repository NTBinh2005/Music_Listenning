package com.project.music_listenning.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    // RestTemplate dùng để gọi HTTP ra ngoài (iTunes API)
    // Cần khai báo @Bean vì Spring không tự tạo
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}