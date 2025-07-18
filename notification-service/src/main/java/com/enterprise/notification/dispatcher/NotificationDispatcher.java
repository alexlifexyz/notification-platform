package com.enterprise.notification.dispatcher;

import com.enterprise.notification.entity.NotificationTemplate;
import com.enterprise.notification.sender.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通知分发器 - 基于策略模式
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class NotificationDispatcher {

    private final Map<String, NotificationSender> senderMap = new ConcurrentHashMap<>();

    /**
     * 自动注入所有NotificationSender实现
     */
    @Autowired
    public NotificationDispatcher(List<NotificationSender> senders) {
        for (NotificationSender sender : senders) {
            String channelCode = sender.getSupportedChannel();
            senderMap.put(channelCode, sender);
            log.info("注册通知发送器: channel={}, sender={}", channelCode, sender.getClass().getSimpleName());
        }
    }

    /**
     * 分发通知
     *
     * @param template        通知模板
     * @param recipientInfo   接收者信息
     * @param renderedSubject 渲染后的主题
     * @param renderedContent 渲染后的内容
     * @param templateParams  模板参数
     * @return 发送结果
     */
    public NotificationSender.SendResult dispatch(NotificationTemplate template,
                                                 NotificationSender.RecipientInfo recipientInfo,
                                                 String renderedSubject,
                                                 String renderedContent,
                                                 Map<String, Object> templateParams) {
        
        String channelCode = template.getChannelCode();
        log.info("开始分发通知: channel={}, recipient={}", channelCode, recipientInfo.getUserId());

        // 获取对应的发送器
        NotificationSender sender = senderMap.get(channelCode);
        if (sender == null) {
            log.error("未找到支持的通知发送器: channel={}", channelCode);
            return new NotificationSender.SendResult(false, "UNKNOWN", 
                    "不支持的通知渠道: " + channelCode);
        }

        try {
            // 执行发送
            NotificationSender.SendResult result = sender.send(template, recipientInfo, 
                    renderedSubject, renderedContent, templateParams);
            
            log.info("通知分发完成: channel={}, recipient={}, success={}", 
                    channelCode, recipientInfo.getUserId(), result.isSuccess());
            
            return result;
        } catch (Exception e) {
            log.error("通知分发异常: channel={}, recipient={}", 
                     channelCode, recipientInfo.getUserId(), e);
            return new NotificationSender.SendResult(false, "UNKNOWN", 
                    "分发异常: " + e.getMessage());
        }
    }

    /**
     * 检查是否支持指定渠道
     *
     * @param channelCode 渠道代码
     * @return 是否支持
     */
    public boolean isChannelSupported(String channelCode) {
        return senderMap.containsKey(channelCode);
    }

    /**
     * 获取所有支持的渠道
     *
     * @return 支持的渠道列表
     */
    public java.util.Set<String> getSupportedChannels() {
        return senderMap.keySet();
    }
}
