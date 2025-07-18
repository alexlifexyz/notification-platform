package com.enterprise.notification.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 通知管理后台启动类
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@SpringBootApplication
@MapperScan("com.enterprise.notification.admin.mapper")
@EnableConfigurationProperties
public class NotificationAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationAdminApplication.class, args);
    }
}
