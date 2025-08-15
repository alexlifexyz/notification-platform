package com.enterprise.notification.admin.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
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

    @Value("${server.port:8081}")
    private String serverPort;

    @Value("${server.servlet.context-path:/notification-admin}")
    private String contextPath;

    @Bean
    public OpenAPI notificationAdminOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("企业级通知平台 - 管理后台API")
                        .description(buildDescription())
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
                                .url("https://admin.company.com/notification-admin")
                                .description("生产环境")
                ))
                .components(new Components()
                        .addResponses("BadRequest", new ApiResponse()
                                .description("请求参数错误")
                                .content(new Content()
                                        .addMediaType("application/json", new MediaType()
                                                .schema(new Schema<>()
                                                        .type("object")
                                                        .addProperty("code", new Schema<>().type("integer").example(400))
                                                        .addProperty("message", new Schema<>().type("string").example("参数验证失败"))
                                                        .addProperty("timestamp", new Schema<>().type("string").format("date-time"))))))
                        .addResponses("NotFound", new ApiResponse()
                                .description("资源不存在")
                                .content(new Content()
                                        .addMediaType("application/json", new MediaType()
                                                .schema(new Schema<>()
                                                        .type("object")
                                                        .addProperty("code", new Schema<>().type("integer").example(404))
                                                        .addProperty("message", new Schema<>().type("string").example("资源不存在"))
                                                        .addProperty("timestamp", new Schema<>().type("string").format("date-time"))))))
                        .addResponses("InternalServerError", new ApiResponse()
                                .description("服务器内部错误")
                                .content(new Content()
                                        .addMediaType("application/json", new MediaType()
                                                .schema(new Schema<>()
                                                        .type("object")
                                                        .addProperty("code", new Schema<>().type("integer").example(500))
                                                        .addProperty("message", new Schema<>().type("string").example("服务器内部错误"))
                                                        .addProperty("timestamp", new Schema<>().type("string").format("date-time")))))));
    }

    private String buildDescription() {
        return "<h1>企业级通知平台 - 管理后台API</h1>\n" ;
    }
}
