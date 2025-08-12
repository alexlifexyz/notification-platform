# 企业通知平台

一个功能完整的企业级通知平台，支持邮件、短信、IM、站内信等多种通知渠道。

## 🚀 快速开始

```bash
# 1. 初始化数据库
./scripts/database-manager.sh init

# 2. 启动服务
./scripts/deployment/start-services-for-swagger-test.sh

# 3. 运行测试验证
./scripts/run-tests.sh basic

# 4. 访问系统
# Swagger UI: http://localhost:8080/notification-service/swagger-ui.html
# Admin API: http://localhost:8081/notification-admin/swagger-ui.html
```

## 📚 完整文档

所有详细文档都在 `docs/` 文件夹中：

### 📖 主要文档
- **[📋 文档索引](docs/INDEX.md)** - 所有文档的导航和索引
- **[🎯 项目概览](docs/01-overview.md)** - 项目介绍和架构说明
- **[⚡ 快速开始](docs/02-quick-start.md)** - 快速部署和使用指南
- **[🔧 配置指南](docs/CONFIGURATION-GUIDE.md)** - 详细的配置说明

### 🔗 快速链接
- [API文档](docs/api/) | [开发指南](docs/development/) | [部署指南](docs/deployment/)
- [完整实现总结](docs/COMPLETE-IMPLEMENTATION-SUMMARY.md)

## ✨ 核心特性

- 📧 **邮件通知** - 支持HTML和纯文本邮件，自动格式检测
- 📱 **短信通知** - 支持阿里云和腾讯云短信，手机号验证
- 💬 **IM通知** - 支持企业微信和钉钉，消息格式化
- 📨 **站内信通知** - 直接存储到数据库，实时可查
- 🔄 **智能降级** - 配置不完整时自动使用Mock模式
- ✅ **完整验证** - 全面的输入验证和错误处理

## 🏗️ 项目结构

```
notification-platform/
├── docs/                    # 📚 所有文档
│   ├── api/                # 📋 API相关文档
│   ├── development/        # 🛠️ 开发相关文档
│   └── deployment/         # 🚀 部署相关文档
├── scripts/                # 🔧 脚本工具
│   ├── tests/             # 🧪 测试脚本
│   ├── database/          # 🗄️ 数据库脚本
│   └── deployment/        # 📦 部署脚本
├── notification-common/    # 📦 公共模块
├── notification-service/   # 🚀 后端服务
├── notification-client-sdk/ # 📱 客户端SDK
├── notification-admin-bff/ # 🔗 管理后台BFF
└── examples/              # 💡 使用示例
```

## 🎯 技术栈

- **后端**: Spring Boot 2.x, MyBatis Plus, MySQL
- **前端**: Vue 3, Element Plus, TypeScript
- **通知**: SMTP邮件, 阿里云/腾讯云短信, 企业微信/钉钉
- **部署**: Docker, Docker Compose

## 📊 当前状态

- ✅ **完全实现** - 所有通知渠道都支持真实发送
- ✅ **Java 8兼容** - 完全兼容Java 8环境
- ✅ **生产就绪** - 可在生产环境中使用
- ✅ **文档完整** - 提供完整的开发和部署文档

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📄 许可证

MIT License

---

💡 **提示**: 查看 [docs/INDEX.md](docs/INDEX.md) 获取完整的文档导航。
