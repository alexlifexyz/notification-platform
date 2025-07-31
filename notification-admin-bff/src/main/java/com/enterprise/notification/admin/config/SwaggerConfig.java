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
        return """
                ## 🛠️ 管理功能

                - **模板管理**: 创建、编辑、删除通知模板，支持变量替换测试
                - **收件人管理**: 管理收件人信息和收件人组
                - **渠道管理**: 配置和管理通知渠道状态
                - **审计监控**: 查看通知发送记录、统计分析、重发失败通知
                - **站内信管理**: 管理站内信的查看、标记、删除等操作

                ## 📊 主要模块

                | 模块 | 功能 | 端点数 |
                |------|------|--------|
                | 模板管理 | CRUD + 测试发送 | 8个 |
                | 收件人管理 | CRUD + 批量导入 | 7个 |
                | 收件人组管理 | CRUD + 成员管理 | 10个 |
                | 渠道管理 | CRUD + 状态切换 | 6个 |
                | 审计监控 | 查询 + 统计 + 重发 | 4个 |
                | 站内信管理 | 查询 + 操作 + 统计 | 6个 |

                ## 🔐 权限说明

                - 所有接口都需要管理员权限
                - 支持分页查询和条件筛选
                - 提供详细的操作日志记录

                ## 🚀 快速开始

                1. 启动核心通知服务
                2. 启动管理后台服务
                3. 访问 Swagger UI 进行接口测试

                详细文档请参考: [GitHub Repository](https://github.com/company/notification-platform)
                """;
    }
}
