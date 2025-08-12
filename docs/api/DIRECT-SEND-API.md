# 直接发送通知API文档

## 概述

直接发送通知API允许您不使用预配置的模板，直接发送指定内容的通知消息。此功能支持所有现有的通知渠道，**支持多渠道同时发送**，并保持与模板发送相同的幂等性和审计功能。

## API接口

### 直接发送通知

**接口**: `POST /api/v1/notifications/send-direct`

**功能**: 不使用模板配置，直接发送指定内容的通知

**请求参数**:

```json
{
  "requestId": "string",        // 必填，请求唯一标识，用于幂等性控制
  "channelCodes": ["string"],   // 必填，通知渠道列表：["IN_APP", "SMS", "EMAIL", "IM"]
  "subject": "string",          // 可选，消息主题/标题
  "content": "string",          // 必填，消息内容
  "recipient": {                // 必填，接收者信息
    "type": "string",           // 必填，接收者类型：individual 或 group
    "id": "string",             // 必填，接收者ID（用户ID或组代码）
    "contactInfo": {            // 可选，联系方式信息
      "userName": "string",     // 用户名称
      "phone": "string",        // 手机号
      "email": "string",        // 邮箱
      "imAccount": "string"     // IM账号
    }
  }
}
```

**响应格式**:

```json
{
  "requestId": "string",
  "status": "SUCCESS|PARTIAL_SUCCESS|FAILED",
  "results": [
    {
      "notificationId": 12345,
      "channelCode": "IN_APP",
      "recipientId": "user123",
      "status": "SUCCESS",
      "sentAt": "2024-07-18T10:30:00"
    }
  ],
  "processedAt": "2024-07-18T10:30:00",
  "errorMessage": "string"  // 仅在失败时返回
}
```

## 使用示例

### 1. 发送站内信

```bash
curl -X POST http://localhost:8080/notification-service/api/v1/notifications/send-direct \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "direct_001",
    "channelCodes": ["IN_APP"],
    "subject": "系统通知",
    "content": "您的账户余额不足，请及时充值。",
    "recipient": {
      "type": "individual",
      "id": "user123",
      "contactInfo": {
        "userName": "张三"
      }
    }
  }'
```

### 2. 发送邮件

```bash
curl -X POST http://localhost:8080/notification-service/api/v1/notifications/send-direct \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "direct_002",
    "channelCodes": ["EMAIL"],
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

### 3. 发送给组

```bash
curl -X POST http://localhost:8080/notification-service/api/v1/notifications/send-direct \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "direct_003",
    "channelCodes": ["IN_APP"],
    "subject": "团队通知",
    "content": "明天上午10点开会，请准时参加。",
    "recipient": {
      "type": "group",
      "id": "DEV_TEAM"
    }
  }'
```

### 4. 多渠道同时发送

```bash
curl -X POST http://localhost:8080/notification-service/api/v1/notifications/send-direct \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "direct_004",
    "channelCodes": ["IN_APP", "EMAIL", "SMS"],
    "subject": "重要紧急通知",
    "content": "系统将在30分钟后进行维护，请及时保存工作。",
    "recipient": {
      "type": "individual",
      "id": "user789",
      "contactInfo": {
        "userName": "王五",
        "email": "wangwu@example.com",
        "phone": "13800138000"
      }
    }
  }'
```

## 特性说明

### 1. 支持的渠道

- **IN_APP**: 站内信
- **SMS**: 短信
- **EMAIL**: 邮件
- **IM**: 企业IM（企业微信、钉钉）

**多渠道支持**：
- 可以在一次请求中指定多个渠道，系统会自动向所有指定渠道发送消息
- 每个渠道的发送结果独立记录，互不影响
- 适用于重要通知需要多渠道触达的场景

### 2. 幂等性保证

- 使用`requestId`确保相同请求不会重复处理
- 重复请求返回首次处理的结果

### 3. 审计记录

- 所有直接发送的消息都会记录在`notifications`表中
- `template_code`字段标记为`DIRECT_SEND`以区分直接发送
- 保持完整的发送状态和错误信息记录

### 4. 错误处理

- 不支持的渠道：返回错误信息
- 接收者不存在：返回错误信息
- 发送失败：记录详细错误信息

## 与模板发送的区别

| 特性 | 模板发送 | 直接发送 |
|------|----------|----------|
| 接口路径 | `/send` | `/send-direct` |
| 模板依赖 | 需要预配置模板 | 无需模板 |
| 内容来源 | 模板渲染 | 直接传入 |
| 渠道指定 | 模板中配置 | 请求中指定 |
| 多渠道支持 | 需要多个模板 | 一次请求支持 |
| 参数验证 | 模板参数验证 | 内容格式验证 |
| 审计标识 | 实际模板代码 | `DIRECT_SEND` |

## 注意事项

1. **内容格式**: 直接发送不进行模板渲染，请确保内容格式正确
2. **渠道限制**: 某些渠道可能对内容长度有限制
3. **联系方式**: 发送邮件和短信时需要提供相应的联系方式
4. **权限控制**: 当前版本无特殊权限限制，请根据业务需求自行控制访问权限
