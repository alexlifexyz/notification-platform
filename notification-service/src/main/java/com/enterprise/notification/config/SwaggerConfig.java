package com.enterprise.notification.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Swagger配置类
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.servlet.context-path:/notification-service}")
    private String contextPath;

    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("企业级通知平台 - 核心服务API")
                        .description("提供统一的消息通知服务，支持多渠道发送、模板渲染、幂等性控制等功能")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Enterprise Team")
                                .email("support@company.com")
                                .url("https://github.com/company/notification-platform"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(Arrays.asList(
                        new Server()
                                .url("http://localhost:" + serverPort + contextPath)
                                .description("本地开发环境"),
                        new Server()
                                .url("https://api.company.com/notification-service")
                                .description("生产环境")
                ));
    }
}
