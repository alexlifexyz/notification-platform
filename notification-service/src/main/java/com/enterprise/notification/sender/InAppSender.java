package com.enterprise.notification.sender;

import com.enterprise.notification.entity.NotificationTemplate;
import com.enterprise.notification.entity.UserInAppMessage;
import com.enterprise.notification.enums.ChannelCode;
import com.enterprise.notification.mapper.UserInAppMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 站内信发送器
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class InAppSender implements NotificationSender {

    @Autowired
    private UserInAppMessageMapper userInAppMessageMapper;

    @Override
    public String getSupportedChannel() {
        return ChannelCode.IN_APP.getCode();
    }

    @Override
    public SendResult send(NotificationTemplate template, 
                          RecipientInfo recipientInfo,
                          String renderedSubject, 
                          String renderedContent,
                          Map<String, Object> templateParams) {
        
        try {
            log.info("开始发送站内信: userId={}, subject={}", 
                    recipientInfo.getUserId(), renderedSubject);

            // 验证接收者信息
            if (!StringUtils.hasText(recipientInfo.getUserId())) {
                return new SendResult(false, "IN_APP", "用户ID不能为空");
            }

            if (!StringUtils.hasText(renderedContent)) {
                return new SendResult(false, "IN_APP", "消息内容不能为空");
            }

            // 创建站内信记录
            UserInAppMessage message = new UserInAppMessage();
            message.setUserId(recipientInfo.getUserId());
            message.setSubject(renderedSubject);
            message.setContent(renderedContent);
            message.setIsRead(false);
            message.setCreatedAt(LocalDateTime.now());
            message.setUpdatedAt(LocalDateTime.now());

            // 保存到数据库
            int result = userInAppMessageMapper.insert(message);
            
            if (result > 0) {
                log.info("站内信发送成功: userId={}, messageId={}", 
                        recipientInfo.getUserId(), message.getId());
                return new SendResult(true, "IN_APP");
            } else {
                log.error("站内信保存失败: userId={}", recipientInfo.getUserId());
                return new SendResult(false, "IN_APP", "数据库保存失败");
            }

        } catch (Exception e) {
            log.error("站内信发送异常: userId={}", recipientInfo.getUserId(), e);
            return new SendResult(false, "IN_APP", "发送异常: " + e.getMessage());
        }
    }
}
