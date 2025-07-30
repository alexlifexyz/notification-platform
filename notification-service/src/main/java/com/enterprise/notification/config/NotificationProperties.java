package com.enterprise.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 通知服务配置属性
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {

    /**
     * 服务提供商配置
     */
    private Providers providers = new Providers();

    /**
     * 模板配置
     */
    private Template template = new Template();

    /**
     * 重试配置
     */
    private Retry retry = new Retry();

    /**
     * 简单邮件配置
     */
    private SimpleEmail email = new SimpleEmail();

    @Data
    public static class Providers {
        /**
         * 短信服务提供商配置
         */
        private Sms sms = new Sms();

        /**
         * 邮件服务提供商配置
         */
        private Email email = new Email();

        /**
         * IM服务提供商配置
         */
        private Im im = new Im();
    }

    @Data
    public static class Sms {
        /**
         * 阿里云短信配置
         */
        private AliyunSms aliyun = new AliyunSms();

        /**
         * 腾讯云短信配置
         */
        private TencentSms tencent = new TencentSms();
    }

    @Data
    public static class AliyunSms {
        private boolean enabled = false;
        private String accessKeyId;
        private String accessKeySecret;
        private String signName;
        private String endpoint;
    }

    @Data
    public static class TencentSms {
        private boolean enabled = false;
        private String secretId;
        private String secretKey;
        private String appId;
    }

    @Data
    public static class Email {
        /**
         * AWS SES配置
         */
        private AwsSes awsSes = new AwsSes();

        /**
         * SendGrid配置
         */
        private SendGrid sendgrid = new SendGrid();
    }

    @Data
    public static class AwsSes {
        private boolean enabled = false;
        private String accessKey;
        private String secretKey;
        private String region;
        private String fromEmail;
    }

    @Data
    public static class SendGrid {
        private boolean enabled = false;
        private String apiKey;
        private String fromEmail;
    }

    @Data
    public static class Im {
        /**
         * 企业微信配置
         */
        private WechatWork wechatWork = new WechatWork();

        /**
         * 钉钉配置
         */
        private DingTalk dingtalk = new DingTalk();
    }

    @Data
    public static class WechatWork {
        private boolean enabled = false;
        private String corpId;
        private String corpSecret;
        private String agentId;
    }

    @Data
    public static class DingTalk {
        private boolean enabled = false;
        private String appKey;
        private String appSecret;
    }

    @Data
    public static class Template {
        /**
         * 变量占位符正则表达式
         */
        private String variablePattern = "\\$\\{([^}]+)\\}";

        /**
         * 是否启用缓存
         */
        private boolean cacheEnabled = true;

        /**
         * 缓存TTL（秒）
         */
        private long cacheTtl = 3600;
    }

    @Data
    public static class Retry {
        /**
         * 最大重试次数
         */
        private int maxAttempts = 3;

        /**
         * 重试延迟（秒）
         */
        private int delaySeconds = 5;
    }

    @Data
    public static class SimpleEmail {
        /**
         * 是否启用邮件发送
         */
        private boolean enabled = true;

        /**
         * 发件人邮箱
         */
        private String from = "noreply@yourcompany.com";

        /**
         * 发件人名称
         */
        private String fromName = "Notification Platform";
    }
}
