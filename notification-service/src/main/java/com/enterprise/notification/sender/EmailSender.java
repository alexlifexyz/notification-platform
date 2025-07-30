package com.enterprise.notification.sender;

import com.enterprise.notification.config.NotificationProperties;
import com.enterprise.notification.entity.NotificationTemplate;
import com.enterprise.notification.enums.ChannelCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.mail.internet.MimeMessage;
import java.util.Map;

/**
 * 邮件发送器
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class EmailSender implements NotificationSender {

    @Autowired
    private NotificationProperties notificationProperties;

    @Autowired(required = false)
    private JavaMailSender javaMailSender;

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
                return new SendResult(false, "EMAIL", "邮箱地址不能为空");
            }

            if (!StringUtils.hasText(renderedContent)) {
                return new SendResult(false, "EMAIL", "邮件内容不能为空");
            }

            // 检查邮件配置是否启用
            if (!notificationProperties.getEmail().isEnabled()) {
                log.warn("邮件发送功能已禁用");
                return new SendResult(false, "EMAIL", "邮件发送功能已禁用");
            }

            // 尝试发送邮件
            boolean sendSuccess = sendEmail(recipientInfo.getEmail(), renderedSubject, renderedContent);

            if (sendSuccess) {
                log.info("邮件发送成功: email={}", recipientInfo.getEmail());
                return new SendResult(true, "EMAIL");
            } else {
                log.error("邮件发送失败: email={}", recipientInfo.getEmail());
                return new SendResult(false, "EMAIL", "邮件发送失败");
            }

        } catch (Exception e) {
            log.error("邮件发送异常: email={}", recipientInfo.getEmail(), e);
            return new SendResult(false, "EMAIL", "发送异常: " + e.getMessage());
        }
    }

    /**
     * 发送邮件
     */
    private boolean sendEmail(String toEmail, String subject, String content) {
        try {
            // 如果没有配置JavaMailSender，使用Mock模式
            if (javaMailSender == null) {
                log.info("JavaMailSender未配置，使用Mock模式发送邮件: email={}, subject={}, content={}",
                        toEmail, subject, content);
                return true;
            }

            // 使用简单邮件发送
            if (isHtmlContent(content)) {
                return sendHtmlEmail(toEmail, subject, content);
            } else {
                return sendSimpleEmail(toEmail, subject, content);
            }

        } catch (Exception e) {
            log.error("邮件发送失败: email={}, subject={}", toEmail, subject, e);
            return false;
        }
    }

    /**
     * 发送简单文本邮件
     */
    private boolean sendSimpleEmail(String toEmail, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(getFromEmail());
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);

            javaMailSender.send(message);
            log.info("简单邮件发送成功: email={}", toEmail);
            return true;

        } catch (Exception e) {
            log.error("简单邮件发送失败: email={}", toEmail, e);
            return false;
        }
    }

    /**
     * 发送HTML邮件
     */
    private boolean sendHtmlEmail(String toEmail, String subject, String content) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(getFromEmail(), notificationProperties.getEmail().getFromName());
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true); // true表示HTML格式

            javaMailSender.send(mimeMessage);
            log.info("HTML邮件发送成功: email={}", toEmail);
            return true;

        } catch (Exception e) {
            log.error("HTML邮件发送失败: email={}", toEmail, e);
            return false;
        }
    }

    /**
     * 判断是否为HTML内容
     */
    private boolean isHtmlContent(String content) {
        return content != null && (content.contains("<html>") || content.contains("<HTML>")
                || content.contains("<body>") || content.contains("<BODY>")
                || content.contains("<div>") || content.contains("<p>"));
    }

    /**
     * 获取发件人邮箱
     */
    private String getFromEmail() {
        return notificationProperties.getEmail().getFrom();
    }
}
