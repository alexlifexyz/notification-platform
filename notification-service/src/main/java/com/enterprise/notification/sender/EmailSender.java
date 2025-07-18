package com.enterprise.notification.sender;

import com.enterprise.notification.config.NotificationProperties;
import com.enterprise.notification.entity.NotificationTemplate;
import com.enterprise.notification.enums.ChannelCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 邮件发送器（Mock实现）
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class EmailSender implements NotificationSender {

    @Autowired
    private NotificationProperties notificationProperties;

    @Override
    public String getSupportedChannel() {
        return ChannelCode.EMAIL.getCode();
    }

    @Override
    public SendResult send(NotificationTemplate template, 
                          RecipientInfo recipientInfo,
                          String renderedSubject, 
                          String renderedContent,
                          Map<String, Object> templateParams) {
        
        try {
            log.info("开始发送邮件: email={}, subject={}", 
                    recipientInfo.getEmail(), renderedSubject);

            // 验证接收者信息
            if (!StringUtils.hasText(recipientInfo.getEmail())) {
                return new SendResult(false, "EMAIL_MOCK", "邮箱地址不能为空");
            }

            if (!StringUtils.hasText(renderedContent)) {
                return new SendResult(false, "EMAIL_MOCK", "邮件内容不能为空");
            }

            // 选择可用的邮件服务商
            String providerCode = selectEmailProvider();
            if (providerCode == null) {
                return new SendResult(false, "EMAIL_MOCK", "没有可用的邮件服务商");
            }

            // Mock发送逻辑
            boolean sendSuccess = mockSendEmail(providerCode, recipientInfo.getEmail(), 
                                              renderedSubject, renderedContent);

            if (sendSuccess) {
                log.info("邮件发送成功: email={}, provider={}", 
                        recipientInfo.getEmail(), providerCode);
                return new SendResult(true, providerCode);
            } else {
                log.error("邮件发送失败: email={}, provider={}", 
                         recipientInfo.getEmail(), providerCode);
                return new SendResult(false, providerCode, "邮件发送失败");
            }

        } catch (Exception e) {
            log.error("邮件发送异常: email={}", recipientInfo.getEmail(), e);
            return new SendResult(false, "EMAIL_MOCK", "发送异常: " + e.getMessage());
        }
    }

    /**
     * 选择可用的邮件服务商
     */
    private String selectEmailProvider() {
        NotificationProperties.Email emailConfig = notificationProperties.getProviders().getEmail();
        
        if (emailConfig.getAwsSes().isEnabled()) {
            return "AWS_SES";
        }
        
        if (emailConfig.getSendgrid().isEnabled()) {
            return "SENDGRID";
        }
        
        return null;
    }

    /**
     * Mock邮件发送
     */
    private boolean mockSendEmail(String providerCode, String email, String subject, String content) {
        // 这里是Mock实现，实际项目中需要调用真实的邮件服务商API
        log.info("Mock邮件发送: provider={}, email={}, subject={}, content={}", 
                providerCode, email, subject, content);
        
        // 模拟发送成功
        return true;
    }
}
