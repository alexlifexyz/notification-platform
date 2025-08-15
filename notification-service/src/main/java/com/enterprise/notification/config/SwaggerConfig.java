package com.enterprise.notification.config;

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

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.servlet.context-path:/notification-service}")
    private String contextPath;

    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("企业级通知平台 - 核心服务API")
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
                                .url("https://api.company.com/notification-service")
                                .description("生产环境")
                ))
                .components(new Components()
                        .addResponses("BadRequest", new ApiResponse()
                                .description("请求参数错误")
                                .content(new Content()
                                        .addMediaType("application/json", new MediaType()
                                                .schema(new Schema<>()
                                                        .type("object")
                                                        .addProperty("status", new Schema<>().type("string").example("FAILED"))
                                                        .addProperty("errorMessage", new Schema<>().type("string").example("参数验证失败"))
                                                        .addProperty("timestamp", new Schema<>().type("string").format("date-time"))))))
                        .addResponses("InternalServerError", new ApiResponse()
                                .description("服务器内部错误")
                                .content(new Content()
                                        .addMediaType("application/json", new MediaType()
                                                .schema(new Schema<>()
                                                        .type("object")
                                                        .addProperty("status", new Schema<>().type("string").example("FAILED"))
                                                        .addProperty("errorMessage", new Schema<>().type("string").example("服务器内部错误"))
                                                        .addProperty("timestamp", new Schema<>().type("string").format("date-time")))))));
    }

    private String buildDescription() {
//        return """
//                ## 🚀 功能特性
//
//                - **多渠道支持**: 邮件、短信、IM（企业微信/钉钉）、站内信
//                - **模板渲染**: 支持动态变量替换和模板缓存
//                - **幂等性控制**: 基于requestId的重复请求检测
//                - **智能降级**: 配置不完整时自动使用Mock模式
//                - **完整验证**: 全面的输入验证和错误处理
//
//                ## 📋 支持的通知渠道
//
//                | 渠道 | 服务商 | 状态 |
//                |------|--------|------|
//                | 邮件 | SMTP/AWS SES/SendGrid | ✅ |
//                | 短信 | 阿里云/腾讯云 | ✅ |
//                | IM | 企业微信/钉钉 | ✅ |
//                | 站内信 | 数据库存储 | ✅ |
//
//                ## 🔧 快速开始
//
//                1. 配置通知服务商密钥
//                2. 创建通知模板
//                3. 调用发送接口
//
//                详细文档请参考: [GitHub Repository](https://github.com/company/notification-platform)
//                """;
        return "企业级通知平台 - 核心服务API";
    }
}
