package com.enterprise.notification.client.config;

import com.enterprise.notification.client.NotificationClient;
import com.enterprise.notification.client.impl.NotificationClientImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 通知客户端自动配置
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(NotificationClientProperties.class)
@ConditionalOnProperty(prefix = "notification.client", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NotificationClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate notificationRestTemplate(NotificationClientProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeout());
        factory.setReadTimeout(properties.getReadTimeout());

        RestTemplate restTemplate = new RestTemplate(factory);
        
        log.info("创建通知客户端RestTemplate: connectTimeout={}, readTimeout={}", 
                properties.getConnectTimeout(), properties.getReadTimeout());
        
        return restTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public NotificationClient notificationClient(RestTemplate notificationRestTemplate, 
                                                NotificationClientProperties properties) {
        log.info("创建通知客户端: baseUrl={}", properties.getBaseUrl());
        return new NotificationClientImpl(notificationRestTemplate, properties);
    }
}
