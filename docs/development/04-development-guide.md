# 开发指南

本指南详细介绍如何在业务系统中集成通知平台，包括客户端SDK使用、业务场景示例和开发最佳实践。

## 🎯 集成概述

通知平台提供两种集成方式：
1. **Java SDK集成**（推荐）- 类型安全、自动配置
2. **HTTP API调用** - 适用于非Java系统

## 📦 Java SDK集成

### 1. 添加依赖

在您的Spring Boot项目中添加依赖：

```xml
<dependency>
    <groupId>com.enterprise</groupId>
    <artifactId>notification-client-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 配置文件

在`application.properties`中添加配置：

```properties
# Spring应用配置
spring.application.name=your-business-service

# Nacos服务发现配置
spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848
spring.cloud.nacos.discovery.enabled=true

# 通知客户端配置
notification.client.enabled=true
# 使用服务名调用，通过Nacos服务发现
notification.client.base-url=http://notification-service/notification-service
notification.client.connect-timeout=5000
notification.client.read-timeout=30000
notification.client.max-retries=3
notification.client.retry-interval=1000
```

### 3. 使用客户端

```java
@Service
public class UserService {
    
    @Autowired
    private NotificationClient notificationClient;
    
    public void registerUser(User user) {
        // 业务逻辑：保存用户
        userRepository.save(user);
        
        // 发送欢迎通知
        sendWelcomeNotification(user);
    }
    
    private void sendWelcomeNotification(User user) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userName", user.getName());
            params.put("registrationTime", LocalDateTime.now().toString());
            
            String requestId = "user_welcome_" + user.getId() + "_" + System.currentTimeMillis();
            
            SendNotificationResponse response = notificationClient.sendToUser(
                requestId,
                "USER_REGISTER_WELCOME",
                user.getId(),
                user.getName(),
                user.getPhone(),
                user.getEmail(),
                params
            );
            
            log.info("欢迎通知发送完成: userId={}, status={}", 
                    user.getId(), response.getStatus());
                    
        } catch (Exception e) {
            log.error("发送欢迎通知失败: userId={}", user.getId(), e);
        }
    }
}
```

## 🎨 业务场景示例

### 1. 用户生命周期通知

#### 用户注册欢迎
```java
@Service
public class UserRegistrationService {
    
    @Autowired
    private NotificationClient notificationClient;
    
    public void handleUserRegistration(String userId, String userName, String email, String phone) {
        // 发送多渠道欢迎消息
        Map<String, Object> params = Map.of(
            "userName", userName,
            "platformName", "企业通知平台",
            "supportEmail", "support@company.com"
        );
        
        String requestId = "user_register_" + userId + "_" + System.currentTimeMillis();
        
        // 发送站内信
        notificationClient.sendToUser(requestId + "_inapp", "USER_REGISTER_WELCOME_INAPP", 
                                     userId, userName, phone, email, params);
        
        // 发送邮件确认
        notificationClient.sendToUser(requestId + "_email", "USER_REGISTER_WELCOME_EMAIL", 
                                     userId, userName, phone, email, params);
    }
}
```

#### 密码重置通知
```java
public void sendPasswordResetNotification(String userId, String email, String resetToken) {
    Map<String, Object> params = Map.of(
        "resetLink", "https://platform.com/reset?token=" + resetToken,
        "expireMinutes", "30",
        "requestTime", LocalDateTime.now().toString()
    );
    
    String requestId = "password_reset_" + userId + "_" + System.currentTimeMillis();
    
    // 同时发送邮件和短信
    notificationClient.sendToUser(requestId + "_email", "PASSWORD_RESET_EMAIL", 
                                 userId, null, null, email, params);
    notificationClient.sendToUser(requestId + "_sms", "PASSWORD_RESET_SMS", 
                                 userId, params);
}
```

### 2. 电商业务通知

#### 订单状态更新
```java
@Service
public class OrderNotificationService {
    
    @Autowired
    private NotificationClient notificationClient;
    
    public void notifyOrderStatusChange(Order order, String newStatus) {
        Map<String, Object> params = Map.of(
            "orderNo", order.getOrderNo(),
            "status", newStatus,
            "statusDescription", getStatusDescription(newStatus),
            "orderDetailLink", "https://shop.com/order/" + order.getOrderNo()
        );
        
        String requestId = "order_status_" + order.getOrderNo() + "_" + System.currentTimeMillis();
        
        notificationClient.sendToUser(
            requestId,
            "ORDER_STATUS_CHANGE",
            order.getUserId(),
            params
        );
    }
    
    public void notifyOrderShipped(Order order, String trackingNo) {
        Map<String, Object> params = Map.of(
            "orderNo", order.getOrderNo(),
            "trackingNo", trackingNo,
            "trackingLink", "https://tracking.com/" + trackingNo,
            "estimatedDelivery", order.getEstimatedDelivery().toString()
        );
        
        String requestId = "order_shipped_" + order.getOrderNo();
        
        notificationClient.sendToUser(
            requestId,
            "ORDER_SHIPPED",
            order.getUserId(),
            params
        );
    }
}
```

#### 库存警告通知
```java
public void sendLowStockAlert(Product product) {
    Map<String, Object> params = Map.of(
        "productName", product.getName(),
        "currentStock", product.getStock(),
        "threshold", product.getLowStockThreshold(),
        "category", product.getCategory()
    );
    
    String requestId = "low_stock_" + product.getSku() + "_" + System.currentTimeMillis();
    
    // 发送给采购团队
    notificationClient.sendToGroup(
        requestId,
        "LOW_STOCK_ALERT",
        "PROCUREMENT_TEAM",
        params
    );
}
```

### 3. 系统运维通知

#### 系统维护公告
```java
@Service
public class SystemMaintenanceService {
    
    public void notifySystemMaintenance(LocalDateTime startTime, String duration, String reason) {
        Map<String, Object> params = Map.of(
            "startTime", startTime.toString(),
            "duration", duration,
            "reason", reason,
            "statusPageLink", "https://status.company.com"
        );
        
        String requestId = "maintenance_" + System.currentTimeMillis();
        
        // 通知所有用户
        notificationClient.sendToGroup(requestId + "_users", "SYSTEM_MAINTENANCE_USER", 
                                      "ALL_USERS", params);
        
        // 通知运维团队
        notificationClient.sendToGroup(requestId + "_ops", "SYSTEM_MAINTENANCE_OPS", 
                                      "OPS_TEAM", params);
    }
}
```

## 🔄 异步处理最佳实践

### 1. 异步发送通知

```java
@Service
public class AsyncNotificationService {
    
    @Autowired
    private NotificationClient notificationClient;
    
    @Async("notificationTaskExecutor")
    public CompletableFuture<Void> sendNotificationAsync(String templateCode, String userId, Map<String, Object> params) {
        return CompletableFuture.runAsync(() -> {
            try {
                String requestId = generateRequestId(templateCode, userId);
                notificationClient.sendToUser(requestId, templateCode, userId, params);
                log.info("异步通知发送成功: templateCode={}, userId={}", templateCode, userId);
            } catch (Exception e) {
                log.error("异步通知发送失败: templateCode={}, userId={}", templateCode, userId, e);
            }
        });
    }
}
```

### 2. 批量通知处理

```java
public void sendBatchNotifications(List<User> users, String templateCode, Map<String, Object> commonParams) {
    // 使用并行流提高处理效率
    users.parallelStream().forEach(user -> {
        try {
            Map<String, Object> userParams = new HashMap<>(commonParams);
            userParams.put("userName", user.getName());
            
            String requestId = generateRequestId(templateCode, user.getId());
            notificationClient.sendToUser(requestId, templateCode, user.getId(), userParams);
            
            // 控制发送频率，避免过载
            Thread.sleep(50);
            
        } catch (Exception e) {
            log.error("批量通知发送失败: userId={}", user.getId(), e);
        }
    });
}
```

## 🛡️ 错误处理和重试

### 1. 异常处理

```java
public void sendNotificationWithRetry(String templateCode, String userId, Map<String, Object> params) {
    int maxRetries = 3;
    int retryCount = 0;
    
    while (retryCount < maxRetries) {
        try {
            String requestId = generateRequestId(templateCode, userId);
            SendNotificationResponse response = notificationClient.sendToUser(
                requestId, templateCode, userId, params);
            
            if ("SUCCESS".equals(response.getStatus())) {
                log.info("通知发送成功: templateCode={}, userId={}", templateCode, userId);
                return;
            } else {
                log.warn("通知发送部分失败: templateCode={}, userId={}, response={}", 
                        templateCode, userId, response);
                return;
            }
            
        } catch (NotificationClientException e) {
            retryCount++;
            log.warn("通知发送失败，准备重试: templateCode={}, userId={}, retry={}/{}, error={}", 
                    templateCode, userId, retryCount, maxRetries, e.getMessage());
            
            if (retryCount >= maxRetries) {
                log.error("通知发送最终失败: templateCode={}, userId={}", templateCode, userId, e);
                // 可以考虑将失败的通知记录到数据库，后续人工处理
                recordFailedNotification(templateCode, userId, params, e.getMessage());
            } else {
                // 等待后重试
                try {
                    Thread.sleep(1000 * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}
```

### 2. 降级处理

```java
@Service
public class NotificationServiceWithFallback {
    
    @Autowired
    private NotificationClient notificationClient;
    
    public void sendNotificationWithFallback(String templateCode, String userId, Map<String, Object> params) {
        try {
            // 首先检查服务健康状态
            if (!notificationClient.isHealthy()) {
                log.warn("通知服务不可用，使用降级方案");
                handleNotificationFallback(templateCode, userId, params);
                return;
            }
            
            // 正常发送
            String requestId = generateRequestId(templateCode, userId);
            notificationClient.sendToUser(requestId, templateCode, userId, params);
            
        } catch (Exception e) {
            log.error("通知发送失败，使用降级方案: templateCode={}, userId={}", templateCode, userId, e);
            handleNotificationFallback(templateCode, userId, params);
        }
    }
    
    private void handleNotificationFallback(String templateCode, String userId, Map<String, Object> params) {
        // 降级方案：记录到数据库，后续补发
        FailedNotification failedNotification = new FailedNotification();
        failedNotification.setTemplateCode(templateCode);
        failedNotification.setUserId(userId);
        failedNotification.setParams(JSON.toJSONString(params));
        failedNotification.setCreatedAt(LocalDateTime.now());
        
        failedNotificationRepository.save(failedNotification);
        log.info("通知已记录到失败队列，等待后续处理: templateCode={}, userId={}", templateCode, userId);
    }
}
```

## 📊 监控和指标

### 1. 健康检查集成

```java
@Component
public class NotificationHealthIndicator implements HealthIndicator {
    
    @Autowired
    private NotificationClient notificationClient;
    
    @Override
    public Health health() {
        try {
            boolean isHealthy = notificationClient.isHealthy();
            if (isHealthy) {
                return Health.up()
                    .withDetail("notification-service", "Available")
                    .build();
            } else {
                return Health.down()
                    .withDetail("notification-service", "Unavailable")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("notification-service", "Error: " + e.getMessage())
                .build();
        }
    }
}
```

### 2. 指标收集

```java
@Service
public class NotificationMetricsService {
    
    private final MeterRegistry meterRegistry;
    private final Counter notificationSentCounter;
    private final Counter notificationFailedCounter;
    private final Timer notificationTimer;
    
    public NotificationMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.notificationSentCounter = Counter.builder("notification.sent")
            .description("Number of notifications sent")
            .register(meterRegistry);
        this.notificationFailedCounter = Counter.builder("notification.failed")
            .description("Number of failed notifications")
            .register(meterRegistry);
        this.notificationTimer = Timer.builder("notification.duration")
            .description("Notification sending duration")
            .register(meterRegistry);
    }
    
    public void sendNotificationWithMetrics(String templateCode, String userId, Map<String, Object> params) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            String requestId = generateRequestId(templateCode, userId);
            notificationClient.sendToUser(requestId, templateCode, userId, params);
            
            notificationSentCounter.increment(
                Tags.of("template", templateCode, "status", "success"));
                
        } catch (Exception e) {
            notificationFailedCounter.increment(
                Tags.of("template", templateCode, "error", e.getClass().getSimpleName()));
            throw e;
        } finally {
            sample.stop(notificationTimer);
        }
    }
}
```

## 🔧 配置管理

### 多环境配置

```yaml
# application.yml
spring:
  profiles:
    active: dev

---
# 开发环境
spring:
  profiles: dev
  
notification:
  client:
    base-url: http://localhost:8080/notification-service
    connect-timeout: 10000
    read-timeout: 60000
    max-retries: 1

logging:
  level:
    com.enterprise.notification: DEBUG

---
# 测试环境
spring:
  profiles: test
  
notification:
  client:
    base-url: http://test-notification:8080/notification-service
    max-retries: 2

---
# 生产环境
spring:
  profiles: prod
  
notification:
  client:
    base-url: http://notification-service:8080/notification-service
    connect-timeout: 3000
    read-timeout: 15000
    max-retries: 5
    retry-interval: 2000

logging:
  level:
    com.enterprise.notification: WARN
```

## 🧪 测试指南

### 单元测试

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private NotificationClient notificationClient;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void shouldSendWelcomeNotificationWhenUserRegisters() {
        // Given
        User user = new User("user123", "张三", "zhangsan@example.com", "13800138000");
        
        when(notificationClient.sendToUser(anyString(), eq("USER_REGISTER_WELCOME"), 
                eq("user123"), any(), any(), any(), any()))
            .thenReturn(createSuccessResponse());
        
        // When
        userService.registerUser(user);
        
        // Then
        verify(notificationClient).sendToUser(
            argThat(requestId -> requestId.startsWith("user_welcome_user123")),
            eq("USER_REGISTER_WELCOME"),
            eq("user123"),
            eq("张三"),
            eq("13800138000"),
            eq("zhangsan@example.com"),
            argThat(params -> "张三".equals(params.get("userName")))
        );
    }
    
    private SendNotificationResponse createSuccessResponse() {
        SendNotificationResponse response = new SendNotificationResponse();
        response.setStatus("SUCCESS");
        return response;
    }
}
```

### 集成测试

```java
@SpringBootTest
@TestPropertySource(properties = {
    "notification.client.base-url=http://localhost:8080/notification-service"
})
class NotificationIntegrationTest {
    
    @Autowired
    private NotificationClient notificationClient;
    
    @Test
    void shouldSendNotificationSuccessfully() {
        // Given
        Map<String, Object> params = Map.of("userName", "测试用户");
        
        // When
        SendNotificationResponse response = notificationClient.sendToUser(
            "integration_test_" + System.currentTimeMillis(),
            "USER_REGISTER_WELCOME",
            "test_user_001",
            params
        );
        
        // Then
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getResults()).isNotEmpty();
    }
}
```

## 🔗 BFF架构说明

### 管理后台BFF服务
管理后台采用BFF (Backend for Frontend) 架构：

- **服务地址**: http://localhost:8081/notification-admin
- **API前缀**: /api/admin
- **数据库**: 直接连接notification-service相同的数据库

### 主要API模块
1. **模板管理**: `/api/admin/templates` - 模板CRUD和测试发送
2. **收件人组管理**: `/api/admin/recipient-groups` - 收件人组管理
3. **发送记录**: `/api/admin/send-records` - 发送历史查询
4. **统计分析**: `/api/admin/statistics` - 发送统计和分析

详细的BFF API文档请查看: http://localhost:8081/notification-admin/swagger-ui.html

## 🎯 最佳实践总结

1. **异步处理**: 使用异步方式发送通知，避免阻塞主业务流程
2. **错误处理**: 实现完善的异常处理和重试机制
3. **降级方案**: 在服务不可用时提供降级处理
4. **监控指标**: 集成健康检查和指标收集
5. **批量优化**: 对于批量通知，使用并行处理提高效率
6. **配置管理**: 使用多环境配置，便于部署管理
7. **测试覆盖**: 编写单元测试和集成测试确保质量

---

**相关文档**: [API参考](./03-api-reference.md) | [部署运维](./05-deployment-guide.md)
