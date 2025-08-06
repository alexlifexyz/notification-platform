# Nacos服务发现集成指南

本文档介绍如何在通知平台中集成Nacos作为服务注册与发现中心。

## 🎯 概述

通知平台已经集成了Nacos服务发现，支持：
- 服务自动注册与发现
- 负载均衡
- 健康检查
- 服务实例管理

## 📦 依赖配置

### 父项目依赖管理

在根目录的 `pom.xml` 中已添加：

```xml
<properties>
    <spring-cloud-alibaba.version>2021.0.5.0</spring-cloud-alibaba.version>
    <spring-cloud.version>2021.0.8</spring-cloud.version>
</properties>

<dependencyManagement>
    <dependencies>
        <!-- Spring Cloud BOM -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>

        <!-- Spring Cloud Alibaba BOM -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-alibaba-dependencies</artifactId>
            <version>${spring-cloud-alibaba.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 服务模块依赖

在 `notification-service` 和 `notification-admin-bff` 的 `pom.xml` 中已添加：

```xml
<!-- Nacos Discovery -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```

### 客户端SDK依赖

在 `notification-client-sdk` 的 `pom.xml` 中已添加：

```xml
<!-- Spring Cloud LoadBalancer (可选，用于负载均衡) -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
    <optional>true</optional>
</dependency>
```

## ⚙️ 配置说明

### notification-service 配置

在 `notification-service/src/main/resources/application.properties` 中：

```properties
# Spring应用配置
spring.application.name=notification-service

# Nacos服务发现配置
spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848
spring.cloud.nacos.discovery.namespace=
spring.cloud.nacos.discovery.group=DEFAULT_GROUP
spring.cloud.nacos.discovery.enabled=true
```

### notification-admin-bff 配置

在 `notification-admin-bff/src/main/resources/application.properties` 中：

```properties
# Spring Application Configuration
spring.application.name=notification-admin-bff

# Nacos Discovery Configuration
spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848
spring.cloud.nacos.discovery.namespace=
spring.cloud.nacos.discovery.group=DEFAULT_GROUP
spring.cloud.nacos.discovery.enabled=true

# Notification Client Configuration
# 使用服务名而不是直接URL，通过Nacos服务发现调用
notification.client.base-url=http://notification-service/notification-service
notification.client.connect-timeout=5000
notification.client.read-timeout=30000
```

### 业务服务集成配置

业务服务集成通知平台时的配置示例：

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
```

## 🚀 启动顺序

1. **启动Nacos Server**
   ```bash
   # 下载并启动Nacos
   wget https://github.com/alibaba/nacos/releases/download/2.2.3/nacos-server-2.2.3.tar.gz
   tar -xzf nacos-server-2.2.3.tar.gz
   cd nacos/bin
   ./startup.sh -m standalone
   ```

2. **启动notification-service**
   ```bash
   cd notification-service
   mvn spring-boot:run
   ```

3. **启动notification-admin-bff**
   ```bash
   cd notification-admin-bff
   mvn spring-boot:run
   ```

4. **启动业务服务**
   ```bash
   cd your-business-service
   mvn spring-boot:run
   ```

## 🔍 验证服务注册

访问Nacos控制台：http://localhost:8848/nacos

默认用户名/密码：nacos/nacos

在"服务管理" -> "服务列表"中应该能看到：
- `notification-service`
- `notification-admin-bff`
- 以及其他业务服务

## 🌍 环境配置

### 开发环境
```properties
spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848
spring.cloud.nacos.discovery.namespace=dev
```

### 测试环境
```properties
spring.cloud.nacos.discovery.server-addr=test-nacos:8848
spring.cloud.nacos.discovery.namespace=test
```

### 生产环境
```properties
spring.cloud.nacos.discovery.server-addr=prod-nacos-cluster:8848
spring.cloud.nacos.discovery.namespace=prod
```

## 🔧 高级配置

### 自定义服务实例信息
```properties
# 自定义服务实例IP（通常不需要）
spring.cloud.nacos.discovery.ip=192.168.1.100
# 自定义服务实例端口（通常不需要）
spring.cloud.nacos.discovery.port=8080
# 服务实例权重
spring.cloud.nacos.discovery.weight=1.0
# 是否为临时实例
spring.cloud.nacos.discovery.ephemeral=true
```

### 健康检查配置
```properties
# 健康检查路径
spring.cloud.nacos.discovery.health-check-path=/actuator/health
# 健康检查间隔
spring.cloud.nacos.discovery.health-check-interval=30s
```

## 🚨 注意事项

1. **服务名一致性**：确保 `spring.application.name` 与客户端配置中的服务名一致
2. **网络连通性**：确保所有服务都能访问Nacos服务器
3. **命名空间隔离**：不同环境使用不同的namespace进行隔离
4. **负载均衡**：客户端SDK自动支持负载均衡，无需额外配置

## 🔄 迁移指南

从直接URL调用迁移到Nacos服务发现：

1. **更新依赖**：添加Nacos相关依赖
2. **修改配置**：将URL改为服务名
3. **添加注解**：在启动类添加 `@EnableDiscoveryClient`
4. **测试验证**：确保服务间调用正常

## 📚 相关文档

- [Nacos官方文档](https://nacos.io/zh-cn/docs/what-is-nacos.html)
- [Spring Cloud Alibaba文档](https://spring-cloud-alibaba-group.github.io/github-pages/hoxton/zh-cn/index.html)
- [通知平台开发指南](./04-development-guide.md)
