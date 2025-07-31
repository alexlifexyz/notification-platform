package com.enterprise.notification.admin;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 通知管理后台启动类
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@SpringBootApplication
@MapperScan("com.enterprise.notification.admin.mapper")
@EnableConfigurationProperties
public class NotificationAdminApplication implements CommandLineRunner {

    @Value("${server.port:8081}")
    private String serverPort;

    @Value("${server.servlet.context-path:/notification-admin}")
    private String contextPath;

    public static void main(String[] args) {
        SpringApplication.run(NotificationAdminApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("\n" +
                "========================================\n" +
                "🛠️ 管理后台启动成功！\n" +
                "📖 Swagger UI: http://localhost:{}{}/swagger-ui.html\n" +
                "========================================",
                serverPort, contextPath
        );
    }
}
