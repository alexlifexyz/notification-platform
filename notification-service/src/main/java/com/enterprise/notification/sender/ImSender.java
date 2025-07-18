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
 * 企业IM发送器（Mock实现）
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class ImSender implements NotificationSender {

    @Autowired
    private NotificationProperties notificationProperties;

    @Override
    public String getSupportedChannel() {
        return ChannelCode.IM.getCode();
    }

    @Override
    public SendResult send(NotificationTemplate template, 
                          RecipientInfo recipientInfo,
                          String renderedSubject, 
                          String renderedContent,
                          Map<String, Object> templateParams) {
        
        try {
            log.info("开始发送IM消息: imAccount={}, content={}", 
                    recipientInfo.getImAccount(), renderedContent);

            // 验证接收者信息
            if (!StringUtils.hasText(recipientInfo.getImAccount())) {
                return new SendResult(false, "IM_MOCK", "IM账号不能为空");
            }

            if (!StringUtils.hasText(renderedContent)) {
                return new SendResult(false, "IM_MOCK", "IM消息内容不能为空");
            }

            // 选择可用的IM服务商
            String providerCode = selectImProvider();
            if (providerCode == null) {
                return new SendResult(false, "IM_MOCK", "没有可用的IM服务商");
            }

            // Mock发送逻辑
            boolean sendSuccess = mockSendIm(providerCode, recipientInfo.getImAccount(), 
                                           renderedSubject, renderedContent);

            if (sendSuccess) {
                log.info("IM消息发送成功: imAccount={}, provider={}", 
                        recipientInfo.getImAccount(), providerCode);
                return new SendResult(true, providerCode);
            } else {
                log.error("IM消息发送失败: imAccount={}, provider={}", 
                         recipientInfo.getImAccount(), providerCode);
                return new SendResult(false, providerCode, "IM消息发送失败");
            }

        } catch (Exception e) {
            log.error("IM消息发送异常: imAccount={}", recipientInfo.getImAccount(), e);
            return new SendResult(false, "IM_MOCK", "发送异常: " + e.getMessage());
        }
    }

    /**
     * 选择可用的IM服务商
     */
    private String selectImProvider() {
        NotificationProperties.Im imConfig = notificationProperties.getProviders().getIm();
        
        if (imConfig.getWechatWork().isEnabled()) {
            return "WECHAT_WORK";
        }
        
        if (imConfig.getDingtalk().isEnabled()) {
            return "DINGTALK";
        }
        
        return null;
    }

    /**
     * Mock IM消息发送
     */
    private boolean mockSendIm(String providerCode, String imAccount, String subject, String content) {
        // 这里是Mock实现，实际项目中需要调用真实的IM服务商API
        log.info("Mock IM消息发送: provider={}, imAccount={}, subject={}, content={}", 
                providerCode, imAccount, subject, content);
        
        // 模拟发送成功
        return true;
    }
}
