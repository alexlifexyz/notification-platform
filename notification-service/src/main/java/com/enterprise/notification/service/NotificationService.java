package com.enterprise.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.notification.dispatcher.NotificationDispatcher;
import com.enterprise.notification.common.dto.DirectSendNotificationRequest;
import com.enterprise.notification.common.dto.SendNotificationRequest;
import com.enterprise.notification.common.dto.SendNotificationResponse;
import com.enterprise.notification.entity.*;
import com.enterprise.notification.enums.RecipientType;
import com.enterprise.notification.enums.SendStatus;
import com.enterprise.notification.mapper.*;
import com.enterprise.notification.sender.NotificationSender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 核心通知服务
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class NotificationService {

    @Autowired
    private NotificationTemplateMapper templateMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private RecipientGroupMapper groupMapper;

    @Autowired
    private RecipientGroupMemberMapper groupMemberMapper;

    @Autowired
    private UserInAppMessageMapper userInAppMessageMapper;

    @Autowired
    private TemplateRenderService templateRenderService;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 发送通知
     *
     * @param request 发送请求
     * @return 发送响应
     */
    @Transactional
    public SendNotificationResponse sendNotification(SendNotificationRequest request) {
        log.info("开始处理通知发送请求: requestId={}, templateCode={}", 
                request.getRequestId(), request.getTemplateCode());

        SendNotificationResponse response = new SendNotificationResponse();
        response.setRequestId(request.getRequestId());
        response.setProcessedAt(LocalDateTime.now());
        response.setResults(new ArrayList<>());

        try {
            // 1. 幂等性检查
            if (isRequestProcessed(request.getRequestId())) {
                log.info("请求已处理，返回幂等结果: requestId={}", request.getRequestId());
                return getExistingResponse(request.getRequestId());
            }

            // 2. 获取模板信息
            NotificationTemplate template = getTemplate(request.getTemplateCode());
            if (template == null) {
                response.setStatus("FAILED");
                response.setErrorMessage("模板不存在: " + request.getTemplateCode());
                return response;
            }

            // 3. 根据接收者类型处理
            List<NotificationSender.RecipientInfo> recipients = resolveRecipients(request.getRecipient());
            if (recipients.isEmpty()) {
                response.setStatus("FAILED");
                response.setErrorMessage("未找到有效的接收者");
                return response;
            }

            // 4. 批量发送
            boolean allSuccess = true;
            for (NotificationSender.RecipientInfo recipient : recipients) {
                SendNotificationResponse.SendResult result = sendToRecipient(
                        request, template, recipient);
                response.getResults().add(result);
                
                if (!"SUCCESS".equals(result.getStatus())) {
                    allSuccess = false;
                }
            }

            response.setStatus(allSuccess ? "SUCCESS" : "PARTIAL_SUCCESS");
            
            log.info("通知发送处理完成: requestId={}, status={}, resultCount={}", 
                    request.getRequestId(), response.getStatus(), response.getResults().size());

        } catch (Exception e) {
            log.error("通知发送处理异常: requestId={}", request.getRequestId(), e);
            response.setStatus("FAILED");
            response.setErrorMessage("处理异常: " + e.getMessage());
        }

        return response;
    }

    /**
     * 直接发送通知（不使用模板）
     *
     * @param request 直接发送请求
     * @return 发送响应
     */
    @Transactional
    public SendNotificationResponse sendDirectNotification(DirectSendNotificationRequest request) {
        log.info("开始处理直接发送通知请求: requestId={}, channelCodes={}",
                request.getRequestId(), request.getChannelCodes());

        SendNotificationResponse response = new SendNotificationResponse();
        response.setRequestId(request.getRequestId());
        response.setProcessedAt(LocalDateTime.now());
        response.setResults(new ArrayList<>());

        try {
            // 1. 幂等性检查
            if (isRequestProcessed(request.getRequestId())) {
                log.info("请求已处理，返回幂等结果: requestId={}", request.getRequestId());
                return getExistingResponse(request.getRequestId());
            }

            // 2. 验证渠道是否支持
            List<String> unsupportedChannels = new ArrayList<>();
            for (String channelCode : request.getChannelCodes()) {
                if (!notificationDispatcher.isChannelSupported(channelCode)) {
                    unsupportedChannels.add(channelCode);
                }
            }
            if (!unsupportedChannels.isEmpty()) {
                response.setStatus("FAILED");
                response.setErrorMessage("不支持的通知渠道: " + String.join(", ", unsupportedChannels));
                return response;
            }

            // 3. 根据接收者类型处理
            List<NotificationSender.RecipientInfo> recipients = resolveRecipients(
                    convertToSendRequestRecipient(request.getRecipient()));
            if (recipients.isEmpty()) {
                response.setStatus("FAILED");
                response.setErrorMessage("未找到有效的接收者");
                return response;
            }

            // 4. 多渠道批量发送
            boolean allSuccess = true;
            for (String channelCode : request.getChannelCodes()) {
                for (NotificationSender.RecipientInfo recipient : recipients) {
                    SendNotificationResponse.SendResult result = sendDirectToRecipient(
                            request, channelCode, recipient);
                    response.getResults().add(result);

                    if (!"SUCCESS".equals(result.getStatus())) {
                        allSuccess = false;
                    }
                }
            }

            response.setStatus(allSuccess ? "SUCCESS" : "PARTIAL_SUCCESS");

            log.info("直接发送通知处理完成: requestId={}, status={}, resultCount={}",
                    request.getRequestId(), response.getStatus(), response.getResults().size());

        } catch (Exception e) {
            log.error("直接发送通知处理异常: requestId={}", request.getRequestId(), e);
            response.setStatus("FAILED");
            response.setErrorMessage("处理异常: " + e.getMessage());
        }

        return response;
    }

    /**
     * 检查请求是否已处理（幂等性）
     */
    private boolean isRequestProcessed(String requestId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getRequestId, requestId);
        return notificationMapper.selectCount(wrapper) > 0;
    }

    /**
     * 获取已存在的响应（幂等性）
     */
    private SendNotificationResponse getExistingResponse(String requestId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getRequestId, requestId);
        List<Notification> notifications = notificationMapper.selectList(wrapper);

        SendNotificationResponse response = new SendNotificationResponse();
        response.setRequestId(requestId);
        response.setProcessedAt(LocalDateTime.now());
        response.setResults(new ArrayList<>());

        boolean allSuccess = true;
        for (Notification notification : notifications) {
            SendNotificationResponse.SendResult result = new SendNotificationResponse.SendResult();
            result.setNotificationId(notification.getId());
            result.setChannelCode(notification.getChannelCode());
            result.setRecipientId(notification.getRecipientId());
            result.setStatus(notification.getSendStatus().name());
            result.setErrorMessage(notification.getErrorMessage());
            result.setSentAt(notification.getSentAt());
            
            response.getResults().add(result);
            
            if (notification.getSendStatus() != SendStatus.SUCCESS) {
                allSuccess = false;
            }
        }

        response.setStatus(allSuccess ? "SUCCESS" : "PARTIAL_SUCCESS");
        return response;
    }

    /**
     * 获取模板
     */
    private NotificationTemplate getTemplate(String templateCode) {
        LambdaQueryWrapper<NotificationTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationTemplate::getTemplateCode, templateCode)
               .eq(NotificationTemplate::getIsEnabled, true);
        return templateMapper.selectOne(wrapper);
    }

    /**
     * 解析接收者
     */
    private List<NotificationSender.RecipientInfo> resolveRecipients(
            SendNotificationRequest.RecipientInfo recipientInfo) {
        
        List<NotificationSender.RecipientInfo> recipients = new ArrayList<>();

        if ("individual".equalsIgnoreCase(recipientInfo.getType())) {
            // 个人接收者
            NotificationSender.RecipientInfo recipient = new NotificationSender.RecipientInfo();
            recipient.setUserId(recipientInfo.getId());
            
            if (recipientInfo.getContactInfo() != null) {
                recipient.setUserName(recipientInfo.getContactInfo().getUserName());
                recipient.setPhone(recipientInfo.getContactInfo().getPhone());
                recipient.setEmail(recipientInfo.getContactInfo().getEmail());
                recipient.setImAccount(recipientInfo.getContactInfo().getImAccount());
            }
            
            recipients.add(recipient);
            
        } else if ("group".equalsIgnoreCase(recipientInfo.getType())) {
            // 组接收者
            recipients = getGroupMembers(recipientInfo.getId());
        }

        return recipients;
    }

    /**
     * 获取组成员
     */
    private List<NotificationSender.RecipientInfo> getGroupMembers(String groupCode) {
        LambdaQueryWrapper<RecipientGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipientGroupMember::getGroupCode, groupCode)
               .eq(RecipientGroupMember::getIsEnabled, true);
        
        List<RecipientGroupMember> members = groupMemberMapper.selectList(wrapper);
        List<NotificationSender.RecipientInfo> recipients = new ArrayList<>();

        for (RecipientGroupMember member : members) {
            NotificationSender.RecipientInfo recipient = new NotificationSender.RecipientInfo();
            recipient.setUserId(member.getUserId());
            recipient.setUserName(member.getUserName());
            recipient.setPhone(member.getPhone());
            recipient.setEmail(member.getEmail());
            recipient.setImAccount(member.getImAccount());
            recipients.add(recipient);
        }

        return recipients;
    }

    /**
     * 发送给单个接收者
     */
    private SendNotificationResponse.SendResult sendToRecipient(
            SendNotificationRequest request,
            NotificationTemplate template,
            NotificationSender.RecipientInfo recipient) {

        SendNotificationResponse.SendResult result = new SendNotificationResponse.SendResult();
        result.setChannelCode(template.getChannelCode());
        result.setRecipientId(recipient.getUserId());

        try {
            // 1. 渲染模板
            String renderedSubject = templateRenderService.render(template.getSubject(), request.getTemplateParams());
            String renderedContent = templateRenderService.render(template.getContent(), request.getTemplateParams());

            // 2. 创建通知记录
            Notification notification = createNotificationRecord(request, template, recipient, 
                    renderedSubject, renderedContent);

            // 3. 分发发送
            NotificationSender.SendResult sendResult = notificationDispatcher.dispatch(
                    template, recipient, renderedSubject, renderedContent, request.getTemplateParams());

            // 4. 更新发送结果
            updateNotificationResult(notification, sendResult);

            // 5. 处理站内信
            if ("IN_APP".equals(template.getChannelCode()) && sendResult.isSuccess()) {
                updateInAppMessageNotificationId(recipient.getUserId(), renderedSubject, notification.getId());
            }

            result.setNotificationId(notification.getId());
            result.setStatus(sendResult.isSuccess() ? "SUCCESS" : "FAILED");
            result.setErrorMessage(sendResult.getErrorMessage());
            result.setSentAt(notification.getSentAt());

        } catch (Exception e) {
            log.error("发送给接收者失败: recipientId={}", recipient.getUserId(), e);
            result.setStatus("FAILED");
            result.setErrorMessage("发送异常: " + e.getMessage());
        }

        return result;
    }

    /**
     * 创建通知记录
     */
    private Notification createNotificationRecord(SendNotificationRequest request,
                                                NotificationTemplate template,
                                                NotificationSender.RecipientInfo recipient,
                                                String renderedSubject,
                                                String renderedContent) {
        Notification notification = new Notification();
        notification.setRequestId(request.getRequestId());
        notification.setTemplateCode(request.getTemplateCode());
        notification.setChannelCode(template.getChannelCode());
        notification.setRecipientType("group".equalsIgnoreCase(request.getRecipient().getType()) ? 
                RecipientType.GROUP : RecipientType.INDIVIDUAL);
        notification.setRecipientId(recipient.getUserId());
        
        try {
            notification.setRecipientInfo(objectMapper.writeValueAsString(recipient));
            notification.setTemplateParams(objectMapper.writeValueAsString(request.getTemplateParams()));
        } catch (JsonProcessingException e) {
            log.warn("JSON序列化失败", e);
        }
        
        notification.setRenderedSubject(renderedSubject);
        notification.setRenderedContent(renderedContent);
        notification.setSendStatus(SendStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());

        notificationMapper.insert(notification);
        return notification;
    }

    /**
     * 更新通知发送结果
     */
    private void updateNotificationResult(Notification notification, NotificationSender.SendResult sendResult) {
        notification.setProviderCode(sendResult.getProviderCode());
        notification.setSendStatus(sendResult.isSuccess() ? SendStatus.SUCCESS : SendStatus.FAILED);
        notification.setErrorMessage(sendResult.getErrorMessage());
        notification.setSentAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());

        notificationMapper.updateById(notification);
    }

    /**
     * 更新站内信的通知ID关联
     */
    private void updateInAppMessageNotificationId(String userId, String subject, Long notificationId) {
        LambdaQueryWrapper<UserInAppMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInAppMessage::getUserId, userId)
               .eq(UserInAppMessage::getSubject, subject)
               .isNull(UserInAppMessage::getNotificationId)
               .orderByDesc(UserInAppMessage::getCreatedAt)
               .last("LIMIT 1");

        UserInAppMessage message = userInAppMessageMapper.selectOne(wrapper);
        if (message != null) {
            message.setNotificationId(notificationId);
            message.setUpdatedAt(LocalDateTime.now());
            userInAppMessageMapper.updateById(message);
        }
    }

    /**
     * 转换DirectSendNotificationRequest的RecipientInfo为SendNotificationRequest的RecipientInfo
     */
    private SendNotificationRequest.RecipientInfo convertToSendRequestRecipient(
            DirectSendNotificationRequest.RecipientInfo directRecipient) {
        SendNotificationRequest.RecipientInfo recipient = new SendNotificationRequest.RecipientInfo();
        recipient.setType(directRecipient.getType());
        recipient.setId(directRecipient.getId());

        if (directRecipient.getContactInfo() != null) {
            SendNotificationRequest.ContactInfo contactInfo = new SendNotificationRequest.ContactInfo();
            contactInfo.setUserName(directRecipient.getContactInfo().getUserName());
            contactInfo.setPhone(directRecipient.getContactInfo().getPhone());
            contactInfo.setEmail(directRecipient.getContactInfo().getEmail());
            contactInfo.setImAccount(directRecipient.getContactInfo().getImAccount());
            recipient.setContactInfo(contactInfo);
        }

        return recipient;
    }

    /**
     * 直接发送给单个接收者的单个渠道
     */
    private SendNotificationResponse.SendResult sendDirectToRecipient(
            DirectSendNotificationRequest request,
            String channelCode,
            NotificationSender.RecipientInfo recipient) {

        SendNotificationResponse.SendResult result = new SendNotificationResponse.SendResult();
        result.setChannelCode(channelCode);
        result.setRecipientId(recipient.getUserId());

        try {
            // 1. 创建通知记录（直接发送）
            Notification notification = createDirectNotificationRecord(request, channelCode, recipient);

            // 2. 创建虚拟模板用于发送
            NotificationTemplate virtualTemplate = createVirtualTemplate(request, channelCode);

            // 3. 分发发送
            NotificationSender.SendResult sendResult = notificationDispatcher.dispatch(
                    virtualTemplate, recipient, request.getSubject(), request.getContent(), null);

            // 4. 更新发送结果
            updateNotificationResult(notification, sendResult);

            // 5. 处理站内信
            if ("IN_APP".equals(channelCode) && sendResult.isSuccess()) {
                updateInAppMessageNotificationId(recipient.getUserId(), request.getSubject(), notification.getId());
            }

            result.setNotificationId(notification.getId());
            result.setStatus(sendResult.isSuccess() ? "SUCCESS" : "FAILED");
            result.setErrorMessage(sendResult.getErrorMessage());
            result.setSentAt(notification.getSentAt());

        } catch (Exception e) {
            log.error("直接发送给接收者失败: recipientId={}", recipient.getUserId(), e);
            result.setStatus("FAILED");
            result.setErrorMessage("发送异常: " + e.getMessage());
        }

        return result;
    }

    /**
     * 创建直接发送的通知记录
     */
    private Notification createDirectNotificationRecord(DirectSendNotificationRequest request,
                                                       String channelCode,
                                                       NotificationSender.RecipientInfo recipient) {
        Notification notification = new Notification();
        notification.setRequestId(request.getRequestId());
        notification.setTemplateCode("DIRECT_SEND"); // 标记为直接发送
        notification.setChannelCode(channelCode);
        notification.setRecipientType("group".equalsIgnoreCase(request.getRecipient().getType()) ?
                RecipientType.GROUP : RecipientType.INDIVIDUAL);
        notification.setRecipientId(recipient.getUserId());

        try {
            notification.setRecipientInfo(objectMapper.writeValueAsString(recipient));
            notification.setTemplateParams("{}"); // 直接发送无模板参数
        } catch (JsonProcessingException e) {
            log.warn("JSON序列化失败", e);
        }

        notification.setRenderedSubject(request.getSubject());
        notification.setRenderedContent(request.getContent());
        notification.setSendStatus(SendStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());

        notificationMapper.insert(notification);
        return notification;
    }

    /**
     * 创建虚拟模板用于直接发送
     */
    private NotificationTemplate createVirtualTemplate(DirectSendNotificationRequest request, String channelCode) {
        NotificationTemplate virtualTemplate = new NotificationTemplate();
        virtualTemplate.setTemplateCode("DIRECT_SEND");
        virtualTemplate.setTemplateName("直接发送");
        virtualTemplate.setChannelCode(channelCode);
        virtualTemplate.setSubject(request.getSubject());
        virtualTemplate.setContent(request.getContent());
        virtualTemplate.setIsEnabled(true);
        return virtualTemplate;
    }
}
