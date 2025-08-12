# 通知平台配置指南

## 📋 配置文件说明

本文档详细说明了 `notification-service/src/main/resources/application.properties` 配置文件中各项配置的含义和使用方法。

## 🔧 必须配置的项目

### 1. 邮件SMTP配置（必须）

要使用邮件通知功能，必须配置以下SMTP参数：

```properties
# SMTP服务器地址
spring.mail.host=smtp.gmail.com
# SMTP端口
spring.mail.port=587
# 邮箱用户名
spring.mail.username=your_email@gmail.com
# 邮箱密码或应用专用密码
spring.mail.password=your_app_password
```

#### 常用邮件服务商配置

| 服务商 | SMTP服务器 | 端口 | 说明 |
|--------|------------|------|------|
| Gmail | smtp.gmail.com | 587 | 需要开启两步验证并使用应用专用密码 |
| QQ邮箱 | smtp.qq.com | 587 | 需要开启SMTP服务并获取授权码 |
| 163邮箱 | smtp.163.com | 587 | 需要开启SMTP服务并获取授权码 |
| 企业邮箱 | 咨询管理员 | 587/25 | 根据企业邮箱配置 |

### 2. 发件人信息配置（必须）

```properties
# 是否启用邮件功能
notification.email.enabled=true
# 发件人邮箱（必须与SMTP用户名一致或为其授权邮箱）
notification.email.from=noreply@yourcompany.com
# 发件人显示名称
notification.email.from-name=企业通知平台
```

## 🔧 可选配置项目

### 1. 数据库配置

默认已配置阿里云RDS，如需修改：

```properties
# 数据库连接URL
spring.datasource.url=jdbc:mysql://your-db-host:3306/your-database
# 数据库用户名
spring.datasource.username=your-username
# 数据库密码
spring.datasource.password=your-password
```

### 2. 短信服务配置

#### 阿里云短信
```properties
# 启用阿里云短信
notification.providers.sms.aliyun.enabled=true
# AccessKey ID
notification.providers.sms.aliyun.access-key-id=your_access_key_id
# AccessKey Secret
notification.providers.sms.aliyun.access-key-secret=your_access_key_secret
# 短信签名
notification.providers.sms.aliyun.sign-name=your_sign_name
```

#### 腾讯云短信
```properties
# 启用腾讯云短信
notification.providers.sms.tencent.enabled=true
# SecretId
notification.providers.sms.tencent.secret-id=your_secret_id
# SecretKey
notification.providers.sms.tencent.secret-key=your_secret_key
# 应用ID
notification.providers.sms.tencent.app-id=your_app_id
```

### 3. 企业IM配置

#### 企业微信
```properties
# 启用企业微信
notification.providers.im.wechat-work.enabled=true
# 企业ID
notification.providers.im.wechat-work.corp-id=your_corp_id
# 应用Secret
notification.providers.im.wechat-work.corp-secret=your_corp_secret
# 应用ID
notification.providers.im.wechat-work.agent-id=your_agent_id
```

#### 钉钉
```properties
# 启用钉钉
notification.providers.im.dingtalk.enabled=true
# 应用Key
notification.providers.im.dingtalk.app-key=your_app_key
# 应用Secret
notification.providers.im.dingtalk.app-secret=your_app_secret
```

## 🚀 快速配置步骤

### 步骤1：配置邮件服务

1. **Gmail配置示例**：
   ```properties
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=yourname@gmail.com
   spring.mail.password=your_app_password
   notification.email.from=yourname@gmail.com
   notification.email.from-name=Your Company
   ```

2. **QQ邮箱配置示例**：
   ```properties
   spring.mail.host=smtp.qq.com
   spring.mail.port=587
   spring.mail.username=yourname@qq.com
   spring.mail.password=your_auth_code
   notification.email.from=yourname@qq.com
   notification.email.from-name=Your Company
   ```

### 步骤2：测试配置

1. 启动服务：
   ```bash
   cd notification-service
   java -jar target/notification-service-1.0.0.jar
   ```

2. 运行测试脚本：
   ```bash
   ./test-email-api.sh
   ```

## ⚠️ 注意事项

### 1. 安全配置
- **不要在代码中硬编码密码**
- **生产环境使用环境变量或配置中心**
- **定期更换密钥和密码**

### 2. 邮件服务商限制
- **Gmail**: 需要开启两步验证并使用应用专用密码
- **QQ/163邮箱**: 需要开启SMTP服务并获取授权码
- **企业邮箱**: 联系管理员获取SMTP配置

### 3. 发送频率限制
- **Gmail**: 每天500封邮件限制
- **QQ邮箱**: 每天50封邮件限制
- **企业邮箱**: 根据企业配置

### 4. 防火墙配置
- 确保服务器可以访问SMTP端口（587/465/25）
- 某些云服务商可能默认封禁25端口

## 🔍 故障排查

### 1. 邮件发送失败
- 检查SMTP配置是否正确
- 检查用户名密码是否正确
- 检查网络连接和防火墙设置
- 查看应用日志获取详细错误信息

### 2. 认证失败
- Gmail: 确认使用应用专用密码而非账户密码
- QQ/163: 确认使用授权码而非登录密码
- 企业邮箱: 确认SMTP服务已开启

### 3. 连接超时
- 检查SMTP服务器地址和端口
- 检查网络连接
- 尝试使用不同的端口（587/465/25）

## 📞 技术支持

如遇到配置问题，请：
1. 查看应用日志文件
2. 检查网络连接
3. 验证邮件服务商设置
4. 联系技术支持团队
