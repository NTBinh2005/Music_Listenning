package com.project.music_listenning.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class AppConfig {

    // RestTemplate dùng để gọi HTTP ra ngoài (iTunes API)
    // Cần khai báo @Bean vì Spring không tự tạo
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // Lấy converter JSON mặc định (MappingJackson2HttpMessageConverter)
        // và thêm text/javascript vào danh sách media type nó xử lý được
        restTemplate.getMessageConverters().forEach(converter -> {
            if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
                List<MediaType> mediaTypes = new ArrayList<>(jacksonConverter.getSupportedMediaTypes());
                mediaTypes.add(MediaType.valueOf("text/javascript"));
                jacksonConverter.setSupportedMediaTypes(mediaTypes);
            }
        });

        return restTemplate;
    }
}