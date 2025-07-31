# Swagger API 文档配置

本文档详细说明了通知平台的 Swagger API 文档配置和使用方法。

## 📋 配置概览

项目已完整集成 SpringDoc OpenAPI 3 (Swagger)，为两个核心服务提供完整的 API 文档：

### 🚀 核心通知服务 (notification-service)
- **端口**: 8080
- **上下文路径**: `/notification-service`
- **Swagger UI**: http://localhost:8080/notification-service/swagger-ui.html
- **API Docs**: http://localhost:8080/notification-service/api-docs

### 🛠️ 管理后台服务 (notification-admin-bff)
- **端口**: 8081
- **上下文路径**: `/notification-admin`
- **Swagger UI**: http://localhost:8081/notification-admin/swagger-ui.html
- **API Docs**: http://localhost:8081/notification-admin/api-docs

## 🔧 配置文件

### 1. Maven 依赖配置

两个服务的 `pom.xml` 都已添加必要依赖：

```xml
<!-- SpringDoc OpenAPI 3 (Swagger) -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-ui</artifactId>
</dependency>

<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-webmvc-core</artifactId>
</dependency>
```

### 2. 应用配置

`application.properties` 中的 Swagger 配置：

```properties
# Swagger API文档配置
# API文档JSON路径
springdoc.api-docs.path=/api-docs
# Swagger UI访问路径
springdoc.swagger-ui.path=/swagger-ui.html
# API操作按方法排序
springdoc.swagger-ui.operationsSorter=method
# API标签按字母排序
springdoc.swagger-ui.tagsSorter=alpha
# 启用"试用"功能
springdoc.swagger-ui.tryItOutEnabled=true
# 启用过滤器
springdoc.swagger-ui.filter=true
```

### 3. Java 配置类

#### 核心服务配置 (`SwaggerConfig.java`)

```java
@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("企业级通知平台 - 核心服务API")
                        .description("提供统一的消息通知服务...")
                        .version("1.0.0"))
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8080/notification-service")
                                   .description("本地开发环境")))
                .components(new Components()
                        .addResponses("BadRequest", ...)
                        .addResponses("InternalServerError", ...));
    }
}
```

## 📖 API 文档特性

### 1. 自动生成文档
- 基于代码注解自动生成 API 文档
- 支持参数验证规则展示
- 自动识别请求/响应模型

### 2. 在线测试功能
- 直接在 Swagger UI 中测试 API
- 支持参数输入和响应查看
- 提供示例请求和响应

### 3. 详细的注解支持

#### 控制器级别注解
```java
@Tag(name = "通知服务", description = "提供统一的消息通知发送服务")
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    // ...
}
```

#### 方法级别注解
```java
@Operation(summary = "发送通知", description = "根据模板代码和接收者信息发送通知")
@PostMapping("/send")
public ResponseEntity<SendNotificationResponse> sendNotification(
        @Parameter(description = "通知发送请求") @Valid @RequestBody SendNotificationRequest request) {
    // ...
}
```

### 4. 全局响应配置
- 统一的错误响应格式
- 标准化的状态码说明
- 详细的错误信息示例

## 🚀 快速开始

### 1. 启动服务

```bash
# 启动核心服务
cd notification-service
mvn spring-boot:run

# 启动管理后台（新终端）
cd notification-admin-bff
mvn spring-boot:run
```

### 2. 访问 Swagger UI

打开浏览器访问：
- 核心服务: http://localhost:8080/notification-service/swagger-ui.html
- 管理后台: http://localhost:8081/notification-admin/swagger-ui.html

### 3. 使用测试脚本

```bash
# 自动启动服务并测试 Swagger
./start-services-for-swagger-test.sh

# 仅测试 Swagger 端点
./test-swagger-endpoints.sh
```

## 📊 API 统计

### 核心通知服务
- **总端点数**: 2个
- **主要功能**: 通知发送、健康检查
- **支持格式**: JSON

### 管理后台服务
- **总端点数**: 41个
- **主要模块**:
  - 模板管理: 8个端点
  - 收件人管理: 7个端点
  - 收件人组管理: 10个端点
  - 渠道管理: 6个端点
  - 审计监控: 4个端点
  - 站内信管理: 6个端点

## 🔍 故障排除

### 1. Swagger UI 无法访问
- 检查服务是否正常启动
- 确认端口没有被占用
- 查看应用日志排查错误

### 2. API 文档不完整
- 检查控制器是否添加了 `@Tag` 注解
- 确认方法是否添加了 `@Operation` 注解
- 验证参数是否添加了 `@Parameter` 注解

### 3. 测试功能异常
- 确认 `tryItOutEnabled=true` 配置
- 检查 CORS 配置是否正确
- 验证请求参数格式是否正确

## 📝 最佳实践

1. **注解完整性**: 为所有公开的 API 添加完整的 Swagger 注解
2. **描述清晰**: 提供清晰、详细的 API 描述和参数说明
3. **示例数据**: 为复杂的请求/响应提供示例数据
4. **版本管理**: 合理使用版本号管理 API 变更
5. **安全配置**: 生产环境中考虑限制 Swagger UI 的访问

## 🔗 相关链接

- [SpringDoc OpenAPI 3 官方文档](https://springdoc.org/)
- [OpenAPI 3.0 规范](https://swagger.io/specification/)
- [项目 API 参考文档](03-api-reference.md)
