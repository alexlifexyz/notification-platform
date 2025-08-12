# 部署运维指南

本指南详细介绍通知平台在生产环境的部署方案、配置管理、监控告警和运维最佳实践。

## 🏗️ 部署架构

### 推荐部署架构

```
┌─────────────────┐    ┌─────────────────┐
│   Load Balancer │    │   Admin UI      │
│   (Nginx/ALB)   │    │   (Vue/React)   │
└─────────────────┘    └─────────────────┘
         │                       │
         ▼                       ▼
┌─────────────────┐    ┌─────────────────┐
│ notification-   │    │ notification-   │
│ service         │    │ admin-bff       │
│ (Multiple)      │    │ (Multiple)      │
└─────────────────┘    └─────────────────┘
         │                       │
         └───────────────────────┼───────────────────────┐
                                 │                       │
                                 ▼                       ▼
                    ┌─────────────────┐    ┌─────────────────┐
                    │     MySQL       │    │   Monitoring    │
                    │   (Master/Slave)│    │ (Prometheus/    │
                    │                 │    │  Grafana)       │
                    └─────────────────┘    └─────────────────┘
```

## 🚀 部署方案

### 方案一：传统部署

#### 1. 环境准备

**服务器要求**:
- **CPU**: 4核心以上
- **内存**: 8GB以上
- **磁盘**: 100GB以上SSD
- **操作系统**: CentOS 7+/Ubuntu 18+

**软件依赖**:
```bash
# 安装JDK 1.8
yum install -y java-1.8.0-openjdk java-1.8.0-openjdk-devel

# 安装MySQL 8.0
yum install -y mysql-server
systemctl start mysqld
systemctl enable mysqld

# 安装Nginx
yum install -y nginx
systemctl start nginx
systemctl enable nginx
```

#### 2. 数据库初始化

```bash
# 创建数据库
mysql -u root -p << EOF
CREATE DATABASE notification_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'notification'@'%' IDENTIFIED BY 'your_strong_password';
GRANT ALL PRIVILEGES ON notification_service.* TO 'notification'@'%';
FLUSH PRIVILEGES;
EOF

# 导入表结构
mysql -u notification -p notification_service < database/notification_service.sql
```

#### 3. 应用部署

```bash
# 创建应用目录
mkdir -p /opt/notification-platform/{service,admin-bff,logs,config}

# 部署核心服务
cp notification-service/target/notification-service-1.0.0.jar /opt/notification-platform/service/
cp notification-admin-bff/target/notification-admin-bff-1.0.0.jar /opt/notification-platform/admin-bff/

# 配置文件
cat > /opt/notification-platform/config/application-prod.properties << EOF
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/notification_service?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=notification
spring.datasource.password=your_strong_password

# 连接池配置
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000

# 日志配置
logging.level.root=WARN
logging.level.com.enterprise.notification=INFO
logging.file.name=/opt/notification-platform/logs/application.log
logging.file.max-size=100MB
logging.file.max-history=30

# JVM参数
server.tomcat.max-threads=200
server.tomcat.min-spare-threads=10
EOF
```

#### 4. 系统服务配置

**notification-service.service**:
```ini
[Unit]
Description=Notification Service
After=network.target mysql.service

[Service]
Type=simple
User=notification
WorkingDirectory=/opt/notification-platform/service
ExecStart=/usr/bin/java -Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 \
  -Dspring.profiles.active=prod \
  -Dspring.config.location=/opt/notification-platform/config/application-prod.properties \
  -jar notification-service-1.0.0.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

**notification-admin-bff.service**:
```ini
[Unit]
Description=Notification Admin BFF
After=network.target mysql.service notification-service.service

[Service]
Type=simple
User=notification
WorkingDirectory=/opt/notification-platform/admin-bff
ExecStart=/usr/bin/java -Xms1g -Xmx2g -XX:+UseG1GC \
  -Dspring.profiles.active=prod \
  -Dspring.config.location=/opt/notification-platform/config/application-prod.properties \
  -jar notification-admin-bff-1.0.0.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

#### 5. 启动服务

```bash
# 创建用户
useradd -r -s /bin/false notification
chown -R notification:notification /opt/notification-platform

# 安装并启动服务
systemctl daemon-reload
systemctl enable notification-service notification-admin-bff
systemctl start notification-service notification-admin-bff

# 检查状态
systemctl status notification-service notification-admin-bff
```

### 方案二：Docker部署

#### 1. Dockerfile

**notification-service/Dockerfile**:
```dockerfile
FROM openjdk:8-jre-alpine

LABEL maintainer="Enterprise Team <team@company.com>"

WORKDIR /app

COPY target/notification-service-1.0.0.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms1g -Xmx2g -XX:+UseG1GC"
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -jar app.jar"]
```

#### 2. Docker Compose

**docker-compose.yml**:
```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: notification-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: notification_service
      MYSQL_USER: notification
      MYSQL_PASSWORD: notification_password
    volumes:
      - mysql_data:/var/lib/mysql
      - ./database/notification_service.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "3306:3306"
    networks:
      - notification-network

  notification-service:
    build: ./notification-service
    container_name: notification-service
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/notification_service?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
      SPRING_DATASOURCE_USERNAME: notification
      SPRING_DATASOURCE_PASSWORD: notification_password
    ports:
      - "8080:8080"
    depends_on:
      - mysql
    networks:
      - notification-network
    restart: unless-stopped

  notification-admin-bff:
    build: ./notification-admin-bff
    container_name: notification-admin-bff
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/notification_service?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
      SPRING_DATASOURCE_USERNAME: notification
      SPRING_DATASOURCE_PASSWORD: notification_password
      NOTIFICATION_CLIENT_BASE_URL: http://notification-service:8080/notification-service
    ports:
      - "8081:8081"
    depends_on:
      - mysql
      - notification-service
    networks:
      - notification-network
    restart: unless-stopped

  nginx:
    image: nginx:alpine
    container_name: notification-nginx
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
    ports:
      - "80:80"
      - "443:443"
    depends_on:
      - notification-service
      - notification-admin-bff
    networks:
      - notification-network
    restart: unless-stopped

volumes:
  mysql_data:

networks:
  notification-network:
    driver: bridge
```

#### 3. 部署命令

```bash
# 构建和启动
docker-compose up -d

# 查看状态
docker-compose ps

# 查看日志
docker-compose logs -f notification-service
```

## ⚙️ 配置管理

### 生产环境配置

**application-prod.yml**:
```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql-master:3306/notification_service?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:notification}
    password: ${DB_PASSWORD:notification_password}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

server:
  port: 8080
  tomcat:
    max-threads: 200
    min-spare-threads: 10
    accept-count: 100

logging:
  level:
    root: WARN
    com.enterprise.notification: INFO
  file:
    name: /var/log/notification/application.log
    max-size: 100MB
    max-history: 30
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{50} - %msg%n"

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    export:
      prometheus:
        enabled: true

# 第三方服务配置
notification:
  providers:
    sms:
      aliyun:
        enabled: true
        access-key-id: ${ALIYUN_ACCESS_KEY_ID}
        access-key-secret: ${ALIYUN_ACCESS_KEY_SECRET}
        region: cn-hangzhou
      tencent:
        enabled: false
    email:
      aws-ses:
        enabled: true
        access-key: ${AWS_ACCESS_KEY}
        secret-key: ${AWS_SECRET_KEY}
        region: us-east-1
    im:
      wechat-work:
        enabled: true
        corp-id: ${WECHAT_CORP_ID}
        corp-secret: ${WECHAT_CORP_SECRET}
```

### 环境变量管理

```bash
# 创建环境变量文件
cat > /opt/notification-platform/config/.env << EOF
# 数据库配置
DB_USERNAME=notification
DB_PASSWORD=your_strong_password

# 第三方服务配置
ALIYUN_ACCESS_KEY_ID=your_aliyun_key_id
ALIYUN_ACCESS_KEY_SECRET=your_aliyun_key_secret

AWS_ACCESS_KEY=your_aws_access_key
AWS_SECRET_KEY=your_aws_secret_key

WECHAT_CORP_ID=your_wechat_corp_id
WECHAT_CORP_SECRET=your_wechat_corp_secret
EOF

# 设置权限
chmod 600 /opt/notification-platform/config/.env
chown notification:notification /opt/notification-platform/config/.env
```

## 📊 监控告警

### 1. Prometheus配置

**prometheus.yml**:
```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'notification-service'
    static_configs:
      - targets: ['notification-service:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s

  - job_name: 'notification-admin-bff'
    static_configs:
      - targets: ['notification-admin-bff:8081']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s
```

### 2. Grafana仪表板

**关键指标**:
- **QPS**: 每秒请求数
- **响应时间**: P95, P99响应时间
- **错误率**: 4xx, 5xx错误率
- **通知发送成功率**: 按渠道统计
- **数据库连接池**: 活跃连接数、等待连接数
- **JVM指标**: 堆内存使用、GC频率

### 3. 告警规则

**alerting.yml**:
```yaml
groups:
  - name: notification-service
    rules:
      - alert: NotificationServiceDown
        expr: up{job="notification-service"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Notification service is down"
          description: "Notification service has been down for more than 1 minute"

      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~"5.."}[5m]) / rate(http_requests_total[5m]) > 0.05
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "High error rate detected"
          description: "Error rate is above 5% for more than 2 minutes"

      - alert: HighResponseTime
        expr: histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m])) > 1
        for: 3m
        labels:
          severity: warning
        annotations:
          summary: "High response time detected"
          description: "95th percentile response time is above 1 second"

      - alert: DatabaseConnectionPoolExhausted
        expr: hikaricp_connections_active / hikaricp_connections_max > 0.9
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Database connection pool nearly exhausted"
          description: "More than 90% of database connections are in use"
```

## 🔧 运维操作

### 1. 日常维护

**日志轮转**:
```bash
# 配置logrotate
cat > /etc/logrotate.d/notification-platform << EOF
/opt/notification-platform/logs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 644 notification notification
    postrotate
        systemctl reload notification-service notification-admin-bff
    endscript
}
EOF
```

**数据库备份**:
```bash
#!/bin/bash
# backup-db.sh
BACKUP_DIR="/opt/backups/notification"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="notification_service_${DATE}.sql"

mkdir -p $BACKUP_DIR

mysqldump -u notification -p notification_service > $BACKUP_DIR/$BACKUP_FILE

# 压缩备份文件
gzip $BACKUP_DIR/$BACKUP_FILE

# 删除7天前的备份
find $BACKUP_DIR -name "*.sql.gz" -mtime +7 -delete

echo "Database backup completed: $BACKUP_FILE.gz"
```

### 2. 性能调优

**JVM参数优化**:
```bash
# 生产环境推荐JVM参数
JAVA_OPTS="-Xms4g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UnlockExperimentalVMOptions \
  -XX:+UseCGroupMemoryLimitForHeap \
  -XX:+PrintGC \
  -XX:+PrintGCDetails \
  -XX:+PrintGCTimeStamps \
  -Xloggc:/opt/notification-platform/logs/gc.log \
  -XX:+UseGCLogFileRotation \
  -XX:NumberOfGCLogFiles=5 \
  -XX:GCLogFileSize=100M"
```

**数据库优化**:
```sql
-- MySQL配置优化
SET GLOBAL innodb_buffer_pool_size = 2147483648; -- 2GB
SET GLOBAL innodb_log_file_size = 268435456;     -- 256MB
SET GLOBAL max_connections = 500;
SET GLOBAL query_cache_size = 134217728;         -- 128MB

-- 索引优化
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notifications_template_code ON notifications(template_code);
CREATE INDEX idx_notifications_recipient_id ON notifications(recipient_id);
CREATE INDEX idx_notifications_request_id ON notifications(request_id);
```

### 3. 故障排查

**常见问题排查**:

```bash
# 检查服务状态
systemctl status notification-service notification-admin-bff

# 查看实时日志
tail -f /opt/notification-platform/logs/application.log

# 检查端口占用
netstat -tlnp | grep -E ':(8080|8081)'

# 检查数据库连接
mysql -u notification -p -e "SELECT 1"

# 检查内存使用
free -h
ps aux | grep java

# 检查磁盘空间
df -h
du -sh /opt/notification-platform/logs/*
```

**性能分析**:
```bash
# JVM堆转储分析
jmap -dump:format=b,file=/tmp/heapdump.hprof $(pgrep -f notification-service)

# 线程分析
jstack $(pgrep -f notification-service) > /tmp/thread-dump.txt

# GC分析
jstat -gc $(pgrep -f notification-service) 5s
```

## 🔒 安全配置

### 1. 网络安全

```bash
# 防火墙配置
firewall-cmd --permanent --add-port=8080/tcp
firewall-cmd --permanent --add-port=8081/tcp
firewall-cmd --reload

# 只允许内网访问数据库
iptables -A INPUT -p tcp --dport 3306 -s 10.0.0.0/8 -j ACCEPT
iptables -A INPUT -p tcp --dport 3306 -j DROP
```

### 2. 应用安全

**SSL/TLS配置**:
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: notification-platform
```

**访问控制**:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: when-authorized
  security:
    enabled: true
```

## 📋 运维检查清单

### 部署前检查
- [ ] 服务器资源充足（CPU、内存、磁盘）
- [ ] 数据库已初始化并可连接
- [ ] 配置文件已正确设置
- [ ] 防火墙规则已配置
- [ ] SSL证书已安装（如需要）

### 部署后验证
- [ ] 服务启动成功
- [ ] 健康检查接口正常
- [ ] 数据库连接正常
- [ ] 日志输出正常
- [ ] 监控指标正常采集
- [ ] 告警规则生效

### 日常运维
- [ ] 检查服务状态
- [ ] 查看错误日志
- [ ] 监控系统资源
- [ ] 检查数据库性能
- [ ] 验证备份完整性

---

**相关文档**: [开发指南](./04-development-guide.md) | [API参考](./03-api-reference.md)
