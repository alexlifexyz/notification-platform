# 异步发送API文档

## 📋 概述

异步发送功能允许业务系统提交通知发送任务后立即返回，通知在后台异步处理。这大大提升了系统的响应速度和并发处理能力。

## ✨ 核心特性

- **立即响应**: 任务提交后立即返回，不等待实际发送完成
- **高并发**: 支持大量并发请求，不阻塞业务流程
- **状态跟踪**: 提供任务状态查询接口，实时了解执行进度
- **任务管理**: 支持任务取消、状态监控等管理功能
- **错误处理**: 完善的异常处理和错误信息反馈
- **资源控制**: 可配置的线程池，合理控制系统资源

## 🚀 API接口

### 1. 异步模板发送

**接口**: `POST /api/v1/notifications/send-async`

**功能**: 异步发送基于模板的通知

**请求参数**: 与同步发送相同的 `SendNotificationRequest`

**响应格式**:
```json
{
  "requestId": "async_001",
  "taskStatus": "SUBMITTED",
  "message": "通知发送任务已提交，正在异步处理中",
  "submittedAt": "2024-07-18T10:30:00",
  "statusUrl": "/api/v1/notifications/async/status/async_001",
  "estimatedCompletionTime": "2024-07-18T10:31:00"
}
```

### 2. 异步直接发送

**接口**: `POST /api/v1/notifications/send-direct-async`

**功能**: 异步直接发送通知，不使用模板

**请求参数**: 与同步直接发送相同的 `DirectSendNotificationRequest`

**响应格式**: 与异步模板发送相同

### 3. 查询任务状态

**接口**: `GET /api/v1/notifications/async/status/{requestId}`

**功能**: 查询异步任务的执行状态和结果

**响应格式**:
```json
{
  "requestId": "async_001",
  "taskStatus": "COMPLETED",
  "startTime": "2024-07-18T10:30:01",
  "endTime": "2024-07-18T10:30:15",
  "errorMessage": null,
  "result": {
    "requestId": "async_001",
    "status": "SUCCESS",
    "results": [
      {
        "notificationId": 12345,
        "channelCode": "EMAIL",
        "recipientId": "user123",
        "status": "SUCCESS",
        "sentAt": "2024-07-18T10:30:15"
      }
    ],
    "processedAt": "2024-07-18T10:30:15"
  },
  "progressInfo": "任务已完成，详细结果请查看日志"
}
```

### 4. 取消任务

**接口**: `DELETE /api/v1/notifications/async/cancel/{requestId}`

**功能**: 取消正在执行或等待执行的异步任务

**响应**: 文本消息，说明取消结果

## 📊 任务状态说明

| 状态 | 说明 |
|------|------|
| `SUBMITTED` | 任务已提交，等待处理 |
| `RUNNING` | 任务正在执行中 |
| `COMPLETED` | 任务执行完成 |
| `FAILED` | 任务执行失败 |
| `CANCELLED` | 任务已被取消 |

## 💡 使用示例

### 异步模板发送

```bash
# 提交异步任务
curl -X POST http://localhost:8080/notification-service/api/v1/notifications/send-async \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "async_welcome_001",
    "templateCode": "USER_WELCOME",
    "recipient": {
      "type": "individual",
      "id": "user123",
      "contactInfo": {
        "userName": "张三",
        "email": "zhangsan@example.com"
      }
    },
    "templateParams": {
      "userName": "张三",
      "welcomeMessage": "欢迎使用我们的服务！"
    }
  }'

# 查询任务状态
curl -X GET http://localhost:8080/notification-service/api/v1/notifications/async/status/async_welcome_001
```

### 异步直接发送

```bash
# 提交异步直接发送任务
curl -X POST http://localhost:8080/notification-service/api/v1/notifications/send-direct-async \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "async_direct_001",
    "channelCodes": ["IN_APP", "EMAIL"],
    "subject": "重要通知",
    "content": "您的订单已发货，请注意查收。",
    "recipient": {
      "type": "individual",
      "id": "user456",
      "contactInfo": {
        "userName": "李四",
        "email": "lisi@example.com"
      }
    }
  }'
```

## ⚙️ 配置说明

### 线程池配置

```properties
# 异步线程池核心线程数
notification.async.core-pool-size=5

# 异步线程池最大线程数
notification.async.max-pool-size=20

# 异步线程池队列容量
notification.async.queue-capacity=100

# 线程空闲时间（秒）
notification.async.keep-alive-seconds=60

# 线程名前缀
notification.async.thread-name-prefix=NotificationAsync-

# 任务清理保留时间（小时）
notification.async.cleanup.retention-hours=24
```

### 配置建议

| 场景 | 核心线程数 | 最大线程数 | 队列容量 |
|------|------------|------------|----------|
| 轻量级 | 2-5 | 10-15 | 50-100 |
| 中等负载 | 5-10 | 20-30 | 100-200 |
| 高负载 | 10-20 | 50-100 | 200-500 |

## 🔍 监控和管理

### 任务统计

系统会定期打印任务统计信息：

```
异步任务统计 - 总数: 150, 运行中: 5, 已完成: 140, 失败: 3, 已取消: 2
```

### 自动清理

- 系统每小时自动清理过期任务记录
- 默认保留24小时的任务历史
- 可通过配置调整保留时间

### 健康检查

异步功能会影响系统健康状态，建议监控：
- 线程池使用率
- 任务队列长度
- 任务失败率
- 平均执行时间

## ⚠️ 注意事项

### 使用建议

1. **合理配置线程池**: 根据系统负载调整线程池参数
2. **监控任务状态**: 定期检查失败任务，及时处理异常
3. **避免长时间任务**: 单个通知任务不应执行过长时间
4. **错误处理**: 客户端应处理任务提交失败的情况

### 限制说明

1. **内存使用**: 任务状态存储在内存中，大量任务会占用内存
2. **任务持久化**: 服务重启会丢失未完成的任务状态
3. **结果获取**: 当前版本任务结果主要通过日志查看

### 最佳实践

1. **批量处理**: 对于大量通知，考虑分批提交
2. **错误重试**: 失败任务可以重新提交
3. **状态轮询**: 客户端可定期查询任务状态
4. **资源监控**: 监控线程池和系统资源使用情况

## 🚀 性能对比

| 指标 | 同步发送 | 异步发送 |
|------|----------|----------|
| 响应时间 | 2-5秒 | 50-100毫秒 |
| 并发能力 | 受限于发送时间 | 受限于线程池配置 |
| 资源占用 | HTTP连接长时间占用 | 快速释放连接 |
| 错误处理 | 立即返回错误 | 需要查询状态获取错误 |
| 适用场景 | 实时性要求高 | 高并发、批量处理 |

异步发送功能显著提升了系统的并发处理能力和用户体验，特别适合高并发和批量通知场景。
