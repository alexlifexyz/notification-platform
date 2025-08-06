package com.enterprise.notification;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 消息通知微服务启动类
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.enterprise.notification.mapper")
@EnableConfigurationProperties
public class NotificationServiceApplication implements CommandLineRunner {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.servlet.context-path:/notification-service}")
    private String contextPath;

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("\n" +
                "========================================\n" +
                "🚀 通知服务启动成功！\n" +
                "📖 Swagger UI: http://localhost:{}{}/swagger-ui.html\n" +
                "========================================",
                serverPort, contextPath
        );
    }
}
