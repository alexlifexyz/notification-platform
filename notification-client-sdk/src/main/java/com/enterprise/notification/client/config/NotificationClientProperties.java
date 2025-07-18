package com.enterprise.notification.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 通知客户端配置属性
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "notification.client")
public class NotificationClientProperties {

    /**
     * 通知服务基础URL
     */
    private String baseUrl = "http://localhost:8080/notification-service";

    /**
     * 连接超时时间（毫秒）
     */
    private int connectTimeout = 5000;

    /**
     * 读取超时时间（毫秒）
     */
    private int readTimeout = 30000;

    /**
     * 最大重试次数
     */
    private int maxRetries = 3;

    /**
     * 重试间隔（毫秒）
     */
    private int retryInterval = 1000;

    /**
     * 是否启用客户端
     */
    private boolean enabled = true;
}
