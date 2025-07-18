package com.enterprise.notification;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 消息通知微服务启动类
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@SpringBootApplication
@MapperScan("com.enterprise.notification.mapper")
@EnableConfigurationProperties
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
