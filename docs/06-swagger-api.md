# Swagger API 文档

本项目已集成 SpringDoc OpenAPI 3 (Swagger)，提供完整的 API 文档和在线测试功能。

##  访问地址

### 核心通知服务 (notification-service)
- **Swagger UI**: http://localhost:8080/notification-service/swagger-ui.html
- **API Docs**: http://localhost:8080/notification-service/api-docs
- **API Docs (JSON)**: http://localhost:8080/notification-service/api-docs.json

### 管理后台服务 (notification-admin-bff)
- **Swagger UI**: http://localhost:8081/notification-admin/swagger-ui.html
- **API Docs**: http://localhost:8081/notification-admin/api-docs
- **API Docs (JSON)**: http://localhost:8081/notification-admin/api-docs.json

##  功能特性

### ✨ 完整的API文档
- **自动生成**: 基于代码注解自动生成API文档
- **实时更新**: 代码变更后文档自动同步
- **详细描述**: 包含请求参数、响应格式、示例等

### ️ 在线测试功能
- **Try it out**: 直接在浏览器中测试API
- **参数填写**: 可视化的参数输入界面
- **响应查看**: 实时查看API响应结果

### ️ 用户友好界面
- **分组展示**: 按功能模块分组显示API
- **搜索过滤**: 支持API搜索和过滤
- **排序功能**: 支持按方法、标签排序

##  API 分组说明

### 核心通知服务 API
```
通知服务
├── POST /api/v1/notifications/send    # 发送通知
└── GET  /api/v1/notifications/health  # 健康检查
```

### 管理后台 API
```
模板管理
├── POST   /api/admin/templates/query      # 分页查询模板
├── GET    /api/admin/templates/{id}       # 获取模板详情
├── POST   /api/admin/templates            # 创建模板
├── PUT    /api/admin/templates/{id}       # 更新模板
├── DELETE /api/admin/templates/{id}       # 删除模板
└── POST   /api/admin/templates/test-send  # 测试发送

收件人组管理
├── POST   /api/admin/recipient-groups/query                    # 分页查询组
├── GET    /api/admin/recipient-groups/{groupCode}              # 获取组详情
├── POST   /api/admin/recipient-groups                          # 创建组
├── PUT    /api/admin/recipient-groups/{groupCode}              # 更新组
├── DELETE /api/admin/recipient-groups/{groupCode}              # 删除组
├── POST   /api/admin/recipient-groups/{groupCode}/members/query # 查询组成员
├── POST   /api/admin/recipient-groups/{groupCode}/members      # 添加成员
├── PUT    /api/admin/recipient-groups/{groupCode}/members/{userId} # 更新成员
└── DELETE /api/admin/recipient-groups/{groupCode}/members/{userId} # 删除成员

通知审计监控
├── POST /api/admin/notifications/query      # 分页查询通知记录
├── GET  /api/admin/notifications/{id}       # 获取通知详情
├── POST /api/admin/notifications/resend     # 重发通知
└── POST /api/admin/notifications/statistics # 获取统计数据
```

##  使用示例

### 1. 发送个人通知
```bash
# 在 Swagger UI 中测试
POST /api/v1/notifications/send

# 请求体示例
{
  "requestId": "user_welcome_001",
  "templateCode": "USER_REGISTER_WELCOME",
  "recipient": {
    "type": "individual",
    "id": "user123"
  },
  "templateParams": {
    "userName": "张三"
  }
}
```

### 2. 发送组通知
```bash
# 在 Swagger UI 中测试
POST /api/v1/notifications/send

# 请求体示例
{
  "requestId": "maintenance_001",
  "templateCode": "SYSTEM_MAINTENANCE",
  "recipient": {
    "type": "group",
    "id": "ALL_EMPLOYEES"
  },
  "templateParams": {
    "startTime": "2024-01-15 02:00:00",
    "duration": "2小时"
  }
}
```

### 3. 查询模板列表
```bash
# 在 Swagger UI 中测试
POST /api/admin/templates/query

# 请求体示例
{
  "current": 1,
  "size": 10,
  "templateCode": "",
  "templateName": "",
  "channelCode": "IN_APP",
  "isEnabled": true
}
```

##  配置说明

### SpringDoc 配置项
```properties
# API文档路径
springdoc.api-docs.path=/api-docs

# Swagger UI路径
springdoc.swagger-ui.path=/swagger-ui.html

# 操作排序方式
springdoc.swagger-ui.operationsSorter=method

# 标签排序方式
springdoc.swagger-ui.tagsSorter=alpha

# 启用Try it out功能
springdoc.swagger-ui.tryItOutEnabled=true

# 启用过滤功能
springdoc.swagger-ui.filter=true
```

##  开发指南

### 添加API文档注解
```java
@Tag(name = "模块名称", description = "模块描述")
@RestController
public class YourController {

    @Operation(summary = "接口摘要", description = "接口详细描述")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功"),
        @ApiResponse(responseCode = "400", description = "请求错误")
    })
    @PostMapping("/your-endpoint")
    public ResponseEntity<YourResponse> yourMethod(
        @Parameter(description = "参数描述") @RequestBody YourRequest request) {
        // 实现逻辑
    }
}
```

### 常用注解说明
- `@Tag`: 标记Controller，用于分组
- `@Operation`: 标记方法，描述API操作
- `@Parameter`: 标记参数，描述参数信息
- `@ApiResponse`: 描述响应信息
- `@Schema`: 描述数据模型

##  注意事项

1. **环境要求**: 确保服务正常启动后再访问Swagger UI
2. **端口配置**: 根据实际配置的端口访问对应地址
3. **数据准备**: 测试前请确保数据库已初始化
4. **权限控制**: 生产环境建议限制Swagger UI的访问权限

##  故障排查

### 常见问题
1. **404错误**: 检查服务是否启动，端口是否正确
2. **空白页面**: 检查SpringDoc依赖是否正确添加
3. **API不显示**: 检查Controller是否添加了正确的注解

### 日志查看
```bash
# 查看服务启动日志
tail -f logs/application.log | grep -i swagger

# 检查SpringDoc配置
curl http://localhost:8080/notification-service/api-docs
```

---

**快速开始**: 启动服务后直接访问 Swagger UI 地址即可开始使用！
