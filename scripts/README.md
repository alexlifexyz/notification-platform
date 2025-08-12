# 脚本工具说明

本目录包含通知平台的各种脚本工具，用于简化开发、测试和部署流程。

## 📁 目录结构

```
scripts/
├── README.md              # 本说明文档
├── run-tests.sh          # 测试脚本统一入口
├── database-manager.sh   # 数据库管理脚本
├── tests/                # 测试脚本目录
│   ├── test-all-notifications.sh
│   ├── test-direct-send-api.sh
│   ├── test-email-api.sh
│   ├── test-multi-channel-idempotency.sh
│   ├── test-multi-channel-send.sh
│   ├── test-new-apis.sh
│   ├── test-simple-startup.sh
│   └── test-swagger-endpoints.sh
├── database/             # 数据库脚本目录
│   ├── notification_service.sql
│   ├── notification_service_updated.sql
│   └── fix_multi_channel_constraint.sql
└── deployment/           # 部署脚本目录
    └── start-services-for-swagger-test.sh
```

## 🧪 测试脚本

### 统一测试入口

```bash
# 显示帮助信息
./scripts/run-tests.sh --help

# 列出所有可用测试
./scripts/run-tests.sh --list

# 运行所有测试
./scripts/run-tests.sh all

# 运行特定测试
./scripts/run-tests.sh direct-send
./scripts/run-tests.sh multi-channel
./scripts/run-tests.sh idempotency
```

### 单独测试脚本

```bash
# 基础功能测试
./scripts/tests/test-simple-startup.sh

# API测试
./scripts/tests/test-direct-send-api.sh
./scripts/tests/test-multi-channel-send.sh
./scripts/tests/test-multi-channel-idempotency.sh

# Swagger测试
./scripts/tests/test-swagger-endpoints.sh
```

## 🗄️ 数据库管理

### 数据库管理脚本

```bash
# 显示帮助信息
./scripts/database-manager.sh --help

# 初始化数据库（全新安装）
MYSQL_PWD=password ./scripts/database-manager.sh init

# 检查数据库状态
./scripts/database-manager.sh status

# 修复多渠道约束问题
./scripts/database-manager.sh fix-multi

# 备份数据库
./scripts/database-manager.sh backup
```

### 环境变量配置

```bash
# 数据库连接配置
export DB_USER=root
export DB_HOST=localhost
export DB_PORT=3306
export MYSQL_PWD=your_password

# 或者在命令行中指定
./scripts/database-manager.sh -u admin -H 192.168.1.100 -P 3306 status
```

## 🚀 部署脚本

### 服务启动

```bash
# 启动所有服务（用于Swagger测试）
./scripts/deployment/start-services-for-swagger-test.sh
```

## 📋 测试场景说明

### 1. 基础功能测试
- **test-simple-startup.sh** - 验证服务启动和基本健康检查
- **test-email-api.sh** - 邮件发送功能测试

### 2. 直接发送功能测试
- **test-direct-send-api.sh** - 直接发送API基础功能测试
- **test-multi-channel-send.sh** - 多渠道发送功能测试
- **test-multi-channel-idempotency.sh** - 多渠道幂等性测试

### 3. 系统集成测试
- **test-all-notifications.sh** - 完整通知流程测试
- **test-swagger-endpoints.sh** - Swagger接口测试
- **test-new-apis.sh** - 新增API功能测试

## 🔧 使用建议

### 开发阶段
```bash
# 1. 初始化数据库
./scripts/database-manager.sh init

# 2. 启动服务
./scripts/deployment/start-services-for-swagger-test.sh

# 3. 运行基础测试
./scripts/run-tests.sh basic
```

### 功能测试
```bash
# 测试直接发送功能
./scripts/run-tests.sh direct-send

# 测试多渠道功能
./scripts/run-tests.sh multi-channel

# 测试幂等性
./scripts/run-tests.sh idempotency
```

### 部署前验证
```bash
# 运行所有测试
./scripts/run-tests.sh all

# 检查数据库状态
./scripts/database-manager.sh status
```

## ⚠️ 注意事项

1. **权限要求**: 所有脚本需要执行权限，使用 `chmod +x` 设置
2. **依赖检查**: 确保系统已安装 MySQL 客户端、curl、jq 等工具
3. **环境配置**: 正确设置数据库连接参数和服务地址
4. **日志查看**: 测试失败时查看详细日志进行问题排查

## 🆘 故障排除

### 常见问题

1. **MySQL连接失败**
   ```bash
   # 检查MySQL服务状态
   systemctl status mysql
   
   # 验证连接参数
   mysql -h localhost -u root -p
   ```

2. **服务启动失败**
   ```bash
   # 检查端口占用
   netstat -tlnp | grep :8080
   
   # 查看服务日志
   tail -f notification-service/logs/application.log
   ```

3. **测试脚本执行失败**
   ```bash
   # 检查脚本权限
   ls -la scripts/tests/
   
   # 手动执行查看详细错误
   bash -x scripts/tests/test-direct-send-api.sh
   ```

## 📞 技术支持

如遇到问题，请：
1. 查看相关日志文件
2. 检查环境配置
3. 参考项目文档
4. 提交Issue描述问题
