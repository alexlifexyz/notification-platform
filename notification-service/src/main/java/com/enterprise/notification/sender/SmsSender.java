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
 * 短信发送器（Mock实现）
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class SmsSender implements NotificationSender {

    @Autowired
    private NotificationProperties notificationProperties;

    @Override
    public String getSupportedChannel() {
        return ChannelCode.SMS.getCode();
    }

    @Override
    public SendResult send(NotificationTemplate template, 
                          RecipientInfo recipientInfo,
                          String renderedSubject, 
                          String renderedContent,
                          Map<String, Object> templateParams) {
        
        try {
            log.info("开始发送短信: phone={}, content={}", 
                    recipientInfo.getPhone(), renderedContent);

            // 验证接收者信息
            if (!StringUtils.hasText(recipientInfo.getPhone())) {
                return new SendResult(false, "SMS_MOCK", "手机号不能为空");
            }

            if (!StringUtils.hasText(renderedContent)) {
                return new SendResult(false, "SMS_MOCK", "短信内容不能为空");
            }

            // 选择可用的短信服务商
            String providerCode = selectSmsProvider();
            if (providerCode == null) {
                return new SendResult(false, "SMS_MOCK", "没有可用的短信服务商");
            }

            // Mock发送逻辑
            boolean sendSuccess = mockSendSms(providerCode, recipientInfo.getPhone(), 
                                            renderedContent, template.getThirdPartyTemplateCode());

            if (sendSuccess) {
                log.info("短信发送成功: phone={}, provider={}", 
                        recipientInfo.getPhone(), providerCode);
                return new SendResult(true, providerCode);
            } else {
                log.error("短信发送失败: phone={}, provider={}", 
                         recipientInfo.getPhone(), providerCode);
                return new SendResult(false, providerCode, "短信发送失败");
            }

        } catch (Exception e) {
            log.error("短信发送异常: phone={}", recipientInfo.getPhone(), e);
            return new SendResult(false, "SMS_MOCK", "发送异常: " + e.getMessage());
        }
    }

    /**
     * 选择可用的短信服务商
     */
    private String selectSmsProvider() {
        NotificationProperties.Sms smsConfig = notificationProperties.getProviders().getSms();
        
        if (smsConfig.getAliyun().isEnabled()) {
            return "ALIYUN_SMS";
        }
        
        if (smsConfig.getTencent().isEnabled()) {
            return "TENCENT_SMS";
        }
        
        return null;
    }

    /**
     * Mock短信发送
     */
    private boolean mockSendSms(String providerCode, String phone, String content, String templateCode) {
        // 这里是Mock实现，实际项目中需要调用真实的短信服务商API
        log.info("Mock短信发送: provider={}, phone={}, templateCode={}, content={}", 
                providerCode, phone, templateCode, content);
        
        // 模拟发送成功
        return true;
    }
}
