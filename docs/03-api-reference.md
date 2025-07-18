# API参考文档

本文档提供通知平台所有API接口的完整参考，包括核心通知服务和管理后台的所有端点。

## 📋 API概览

### 核心通知服务 (notification-service)
- **基础URL**: `http://localhost:8080/notification-service`
- **功能**: 通知发送、健康检查
- **端点数**: 2个

### 管理后台服务 (notification-admin-bff)  
- **基础URL**: `http://localhost:8081/notification-admin`
- **功能**: 模板管理、组管理、审计监控
- **端点数**: 17个

## 🚀 核心通知服务API

### 1. 发送通知

**接口**: `POST /api/v1/notifications/send`

**功能**: 发送通知消息，支持个人和组接收者

**请求体**:
```json
{
  "requestId": "req_20240718_001",
  "templateCode": "USER_WELCOME",
  "recipient": {
    "type": "individual",
    "id": "user123",
    "contactInfo": {
      "userName": "张三",
      "phone": "13800138000",
      "email": "zhangsan@example.com",
      "imAccount": "zhangsan"
    }
  },
  "templateParams": {
    "userName": "张三",
    "productName": "通知平台"
  }
}
```

**请求参数说明**:
- `requestId`: 请求唯一标识，用于幂等性控制
- `templateCode`: 模板代码，必须在系统中已配置
- `recipient.type`: 接收者类型，`individual`（个人）或`group`（组）
- `recipient.id`: 接收者ID，个人用户ID或组代码
- `recipient.contactInfo`: 联系方式（个人接收者时使用）
- `templateParams`: 模板参数，用于渲染模板内容

**响应示例**:
```json
{
  "requestId": "req_20240718_001",
  "status": "SUCCESS",
  "results": [
    {
      "notificationId": 12345,
      "channelCode": "IN_APP",
      "recipientId": "user123",
      "status": "SUCCESS",
      "sentAt": "2024-07-18T10:30:00"
    },
    {
      "notificationId": 12346,
      "channelCode": "SMS",
      "recipientId": "user123", 
      "status": "SUCCESS",
      "sentAt": "2024-07-18T10:30:01"
    }
  ],
  "processedAt": "2024-07-18T10:30:00"
}
```

**状态码**:
- `200`: 成功处理（不代表所有通知都发送成功）
- `400`: 请求参数错误
- `500`: 服务器内部错误

### 2. 健康检查

**接口**: `GET /api/v1/notifications/health`

**功能**: 检查服务健康状态

**响应**: `Notification Service is running`

## 🎛️ 管理后台API

### 模板管理 (6个端点)

#### 1. 查询模板列表

**接口**: `POST /api/admin/templates/query`

**请求体**:
```json
{
  "current": 1,
  "size": 10,
  "templateCode": "USER",
  "templateName": "用户",
  "channelCode": "SMS",
  "isEnabled": true
}
```

**响应**:
```json
{
  "current": 1,
  "size": 10,
  "total": 100,
  "pages": 10,
  "records": [
    {
      "id": 1,
      "templateCode": "USER_WELCOME",
      "templateName": "用户欢迎",
      "channelCode": "SMS",
      "channelName": "短信",
      "subject": null,
      "content": "欢迎${userName}使用我们的服务！",
      "thirdPartyTemplateCode": "SMS_123456",
      "isEnabled": true,
      "createdAt": "2024-07-18T10:00:00",
      "updatedAt": "2024-07-18T10:00:00"
    }
  ]
}
```

#### 2. 获取模板详情

**接口**: `GET /api/admin/templates/{id}`

#### 3. 创建模板

**接口**: `POST /api/admin/templates`

**请求体**:
```json
{
  "templateCode": "ORDER_SHIPPED",
  "templateName": "订单发货通知",
  "channelCode": "SMS",
  "content": "您的订单${orderNo}已发货，预计${deliveryTime}送达。",
  "thirdPartyTemplateCode": "SMS_789012",
  "isEnabled": true
}
```

#### 4. 更新模板

**接口**: `PUT /api/admin/templates/{id}`

#### 5. 删除模板

**接口**: `DELETE /api/admin/templates/{id}`

#### 6. 测试发送模板

**接口**: `POST /api/admin/templates/test-send`

**请求体**:
```json
{
  "templateCode": "ORDER_SHIPPED",
  "testRecipient": {
    "userId": "test_user_001",
    "userName": "测试用户",
    "phone": "13800138000",
    "email": "test@example.com"
  },
  "templateParams": {
    "orderNo": "ORD20240718001",
    "deliveryTime": "明天下午"
  }
}
```

### 收件人组管理 (8个端点)

#### 1. 查询收件人组列表

**接口**: `POST /api/admin/recipient-groups/query`

**请求体**:
```json
{
  "current": 1,
  "size": 10,
  "groupCode": "SALES",
  "groupName": "销售",
  "isEnabled": true
}
```

#### 2. 获取收件人组详情

**接口**: `GET /api/admin/recipient-groups/{groupCode}`

#### 3. 创建收件人组

**接口**: `POST /api/admin/recipient-groups`

**请求体**:
```json
{
  "groupCode": "SALES_TEAM",
  "groupName": "销售团队",
  "description": "负责销售业务的团队成员",
  "isEnabled": true
}
```

#### 4. 更新收件人组

**接口**: `PUT /api/admin/recipient-groups/{groupCode}`

#### 5. 删除收件人组

**接口**: `DELETE /api/admin/recipient-groups/{groupCode}`

#### 6. 查询组成员

**接口**: `POST /api/admin/recipient-groups/members/query`

**请求体**:
```json
{
  "groupCode": "SALES_TEAM",
  "current": 1,
  "size": 10,
  "userId": "sales",
  "userName": "销售",
  "isEnabled": true
}
```

#### 7. 添加组成员

**接口**: `POST /api/admin/recipient-groups/{groupCode}/members`

**请求体**:
```json
{
  "userId": "sales001",
  "userName": "张销售",
  "phone": "13800138001",
  "email": "sales001@company.com",
  "imAccount": "zhangxiaoshou",
  "preferredChannels": ["SMS", "EMAIL", "IM"],
  "isEnabled": true
}
```

#### 8. 更新组成员

**接口**: `PUT /api/admin/recipient-groups/{groupCode}/members/{userId}`

#### 9. 删除组成员

**接口**: `DELETE /api/admin/recipient-groups/{groupCode}/members/{userId}`

### 审计监控 (4个端点)

#### 1. 查询通知记录

**接口**: `POST /api/admin/notifications/query`

**请求体**:
```json
{
  "current": 1,
  "size": 20,
  "requestId": "req_20240718",
  "templateCode": "ORDER_STATUS",
  "recipientId": "user123",
  "status": "SUCCESS",
  "channelCode": "SMS",
  "startTime": "2024-07-18T00:00:00",
  "endTime": "2024-07-18T23:59:59"
}
```

#### 2. 获取通知记录详情

**接口**: `GET /api/admin/notifications/{id}`

#### 3. 重发失败通知

**接口**: `POST /api/admin/notifications/{id}/resend`

**请求体**:
```json
{
  "reason": "网络异常导致发送失败，现在重新发送"
}
```

#### 4. 获取通知统计

**接口**: `POST /api/admin/notifications/statistics`

**请求体**:
```json
{
  "startTime": "2024-07-18T00:00:00",
  "endTime": "2024-07-18T23:59:59",
  "groupBy": "day",
  "channelCode": "SMS",
  "templateCode": "ORDER_STATUS"
}
```

**响应**:
```json
{
  "startTime": "2024-07-18T00:00:00",
  "endTime": "2024-07-18T23:59:59",
  "totalCount": 10000,
  "successCount": 9800,
  "failedCount": 200,
  "successRate": 98.0,
  "channelStatistics": [
    {
      "channelCode": "SMS",
      "channelName": "短信",
      "count": 5000,
      "successCount": 4900,
      "failedCount": 100,
      "successRate": 98.0
    }
  ],
  "templateStatistics": [
    {
      "templateCode": "ORDER_STATUS_CHANGE",
      "templateName": "订单状态变更",
      "count": 3000,
      "successCount": 2950,
      "failedCount": 50,
      "successRate": 98.3
    }
  ],
  "timeSeriesStatistics": [
    {
      "time": "2024-07-18T00:00:00",
      "count": 500,
      "successCount": 490,
      "failedCount": 10
    }
  ]
}
```

## 🔧 通用规范

### 请求格式
- **Content-Type**: `application/json`
- **字符编码**: UTF-8
- **时间格式**: ISO 8601 (`2024-07-18T10:30:00`)

### 响应格式
- **成功响应**: HTTP 200，返回JSON数据
- **错误响应**: 对应的HTTP状态码，返回错误信息

### 错误响应格式
```json
{
  "timestamp": "2024-07-18T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "模板代码已存在: ORDER_STATUS_CHANGE",
  "path": "/api/admin/templates"
}
```

### 分页响应格式
```json
{
  "current": 1,
  "size": 10,
  "total": 100,
  "pages": 10,
  "records": [...]
}
```

## 📝 使用示例

### cURL示例

```bash
# 发送通知
curl -X POST http://localhost:8080/notification-service/api/v1/notifications/send \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "test_001",
    "templateCode": "USER_WELCOME",
    "recipient": {
      "type": "individual",
      "id": "user123"
    },
    "templateParams": {
      "userName": "张三"
    }
  }'

# 查询模板
curl -X POST http://localhost:8081/notification-admin/api/admin/templates/query \
  -H "Content-Type: application/json" \
  -d '{"current":1,"size":10}'

# 创建收件人组
curl -X POST http://localhost:8081/notification-admin/api/admin/recipient-groups \
  -H "Content-Type: application/json" \
  -d '{
    "groupCode": "DEV_TEAM",
    "groupName": "开发团队",
    "description": "产品开发团队",
    "isEnabled": true
  }'
```

### JavaScript示例

```javascript
// 发送通知
const sendNotification = async (data) => {
  const response = await fetch('http://localhost:8080/notification-service/api/v1/notifications/send', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)
  });
  return response.json();
};

// 查询模板
const queryTemplates = async (query) => {
  const response = await fetch('http://localhost:8081/notification-admin/api/admin/templates/query', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(query)
  });
  return response.json();
};
```

---

**相关文档**: [开发指南](./04-development-guide.md) | [部署运维](./05-deployment-guide.md)
