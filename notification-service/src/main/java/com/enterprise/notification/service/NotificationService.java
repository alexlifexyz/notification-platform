package com.enterprise.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.notification.dispatcher.NotificationDispatcher;
import com.enterprise.notification.async.AsyncTaskManager;
import com.enterprise.notification.common.dto.BaseNotificationRequest;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
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

    @Autowired
    private AsyncTaskManager asyncTaskManager;

    /**
     * 发送通知
     *
     * @param request 发送请求
     * @return 发送响应
     */
    @Transactional
    public SendNotificationResponse sendNotification(SendNotificationRequest request) {
        log.info("开始处理通知发送请求: requestId={}, templateCode={}, isGroup={}, userCount={}",
                request.getRequestId(), request.getTemplateCode(), request.isGroupSend(), request.getUserCount());

        SendNotificationResponse response = new SendNotificationResponse();
        response.setRequestId(request.getRequestId());
        response.setProcessedAt(LocalDateTime.now());
        response.setResults(new ArrayList<>());

        try {
            // 1. 获取模板信息（幂等性检查在具体处理方法中进行）
            NotificationTemplate template = getTemplate(request.getTemplateCode());
            if (template == null) {
                response.setStatus("FAILED");
                response.setErrorMessage("模板不存在: " + request.getTemplateCode());
                return response;
            }

            // 3. 处理接收者
            if (request.isGroupSend()) {
                // 组发送
                return handleGroupTemplateSend(request, template);
            } else {
                // 多用户发送
                return handleMultiUserTemplateSend(request, template);
            }

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
        log.info("开始处理直接发送通知请求: requestId={}, channelCodes={}, isGroup={}, userCount={}",
                request.getRequestId(), request.getChannelCodes(), request.isGroupSend(), request.getUserCount());

        SendNotificationResponse response = new SendNotificationResponse();
        response.setRequestId(request.getRequestId());
        response.setProcessedAt(LocalDateTime.now());
        response.setResults(new ArrayList<>());

        try {
            // 1. 验证渠道是否支持
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

            // 2. 处理接收者
            if (request.isGroupSend()) {
                // 组发送
                return handleGroupDirectSend(request);
            } else {
                // 多用户发送
                return handleMultiUserDirectSend(request);
            }



        } catch (Exception e) {
            log.error("直接发送通知处理异常: requestId={}", request.getRequestId(), e);
            response.setStatus("FAILED");
            response.setErrorMessage("处理异常: " + e.getMessage());
        }

        return response;
    }

    /**
     * 异步发送通知（模板方式）
     *
     * @param request 发送请求
     * @return 异步任务Future
     */
    @Async("notificationAsyncExecutor")
    public CompletableFuture<SendNotificationResponse> sendNotificationAsync(SendNotificationRequest request) {
        log.info("开始异步发送通知: requestId={}, templateCode={}",
                request.getRequestId(), request.getTemplateCode());

        return CompletableFuture.supplyAsync(() -> {
            try {
                SendNotificationResponse response = sendNotification(request);
                log.info("异步通知发送完成: requestId={}, status={}",
                        request.getRequestId(), response.getStatus());
                return response;
            } catch (Exception e) {
                log.error("异步通知发送异常: requestId={}", request.getRequestId(), e);

                // 创建错误响应
                SendNotificationResponse errorResponse = new SendNotificationResponse();
                errorResponse.setRequestId(request.getRequestId());
                errorResponse.setStatus("FAILED");
                errorResponse.setErrorMessage("异步发送异常: " + e.getMessage());
                errorResponse.setProcessedAt(LocalDateTime.now());
                errorResponse.setResults(new ArrayList<>());

                return errorResponse;
            }
        });
    }

    /**
     * 异步发送通知（直接发送方式）
     *
     * @param request 直接发送请求
     * @return 异步任务Future
     */
    @Async("notificationAsyncExecutor")
    public CompletableFuture<SendNotificationResponse> sendDirectNotificationAsync(DirectSendNotificationRequest request) {
        log.info("开始异步直接发送通知: requestId={}, channelCodes={}",
                request.getRequestId(), request.getChannelCodes());

        return CompletableFuture.supplyAsync(() -> {
            try {
                SendNotificationResponse response = sendDirectNotification(request);
                log.info("异步直接发送完成: requestId={}, status={}",
                        request.getRequestId(), response.getStatus());
                return response;
            } catch (Exception e) {
                log.error("异步直接发送异常: requestId={}", request.getRequestId(), e);

                // 创建错误响应
                SendNotificationResponse errorResponse = new SendNotificationResponse();
                errorResponse.setRequestId(request.getRequestId());
                errorResponse.setStatus("FAILED");
                errorResponse.setErrorMessage("异步发送异常: " + e.getMessage());
                errorResponse.setProcessedAt(LocalDateTime.now());
                errorResponse.setResults(new ArrayList<>());

                return errorResponse;
            }
        });
    }

    /**
     * 处理组直接发送
     */
    private SendNotificationResponse handleGroupDirectSend(DirectSendNotificationRequest request) {
        log.info("处理组直接发送: requestId={}, groupCode={}", request.getRequestId(), request.getGroupCode());

        SendNotificationResponse response = new SendNotificationResponse();
        response.setRequestId(request.getRequestId());
        response.setProcessedAt(LocalDateTime.now());
        response.setResults(new ArrayList<>());

        try {
            boolean allSuccess = true;

            // 获取组成员
            List<RecipientGroupMember> groupMembers = getGroupMembers(request.getGroupCode());
            if (groupMembers.isEmpty()) {
                response.setStatus("FAILED");
                response.setErrorMessage("组成员为空: " + request.getGroupCode());
                return response;
            }

            // 为每个组成员的每个渠道发送
            for (RecipientGroupMember member : groupMembers) {
                for (String channelCode : request.getChannelCodes()) {
                    // 检查该用户渠道是否已处理过
                    if (isRequestUserChannelProcessed(request.getRequestId(), member.getUserId(), channelCode)) {
                        log.info("用户渠道请求已处理，跳过: requestId={}, userId={}, channelCode={}",
                                request.getRequestId(), member.getUserId(), channelCode);
                        continue;
                    }

                    // 创建组成员接收者信息
                    NotificationSender.RecipientInfo memberRecipient = new NotificationSender.RecipientInfo();
                    memberRecipient.setType("group");  // 直接设置为组类型
                    memberRecipient.setUserId(member.getUserId());  // 语义明确的userId字段
                    memberRecipient.setUserName(member.getUserName());
                    memberRecipient.setEmail(member.getEmail());
                    memberRecipient.setPhone(member.getPhone());
                    memberRecipient.setImAccount(member.getImAccount());

                    // 发送到指定渠道
                    SendNotificationResponse.SendResult result = sendDirectToRecipient(
                            request, channelCode, memberRecipient);
                    response.getResults().add(result);

                    if (!"SUCCESS".equals(result.getStatus())) {
                        allSuccess = false;
                    }
                }
            }

            response.setStatus(allSuccess ? "SUCCESS" : "PARTIAL_SUCCESS");
            return response;

        } catch (Exception e) {
            log.error("组直接发送异常: requestId={}", request.getRequestId(), e);
            response.setStatus("FAILED");
            response.setErrorMessage("组发送异常: " + e.getMessage());
            return response;
        }
    }

    /**
     * 处理多用户直接发送
     */
    private SendNotificationResponse handleMultiUserDirectSend(DirectSendNotificationRequest request) {
        log.info("处理多用户直接发送: requestId={}, userCount={}", request.getRequestId(), request.getUserCount());

        SendNotificationResponse response = new SendNotificationResponse();
        response.setRequestId(request.getRequestId());
        response.setProcessedAt(LocalDateTime.now());
        response.setResults(new ArrayList<>());

        try {
            boolean allSuccess = true;

            // 为每个用户的每个渠道发送
            for (BaseNotificationRequest.UserInfo user : request.getUsers()) {
                for (String channelCode : request.getChannelCodes()) {
                    // 检查该用户渠道是否已处理过
                    if (isRequestUserChannelProcessed(request.getRequestId(), user.getUserId(), channelCode)) {
                        log.info("用户渠道请求已处理，跳过: requestId={}, userId={}, channelCode={}",
                                request.getRequestId(), user.getUserId(), channelCode);
                        continue;
                    }

                    // 创建个人接收者信息
                    NotificationSender.RecipientInfo recipient = new NotificationSender.RecipientInfo();
                    recipient.setType("individual");
                    recipient.setUserId(user.getUserId());  // 语义明确的userId字段
                    recipient.setUserName(user.getUserName());
                    recipient.setEmail(user.getEmail());
                    recipient.setPhone(user.getPhone());
                    recipient.setImAccount(user.getImAccount());

                    // 发送到指定渠道
                    SendNotificationResponse.SendResult result = sendDirectToRecipient(
                            request, channelCode, recipient);
                    response.getResults().add(result);

                    if (!"SUCCESS".equals(result.getStatus())) {
                        allSuccess = false;
                    }
                }
            }

            response.setStatus(allSuccess ? "SUCCESS" : "PARTIAL_SUCCESS");
            return response;

        } catch (Exception e) {
            log.error("多用户直接发送异常: requestId={}", request.getRequestId(), e);
            response.setStatus("FAILED");
            response.setErrorMessage("多用户发送异常: " + e.getMessage());
            return response;
        }
    }

    /**
     * 处理组模板发送
     */
    private SendNotificationResponse handleGroupTemplateSend(SendNotificationRequest request, NotificationTemplate template) {
        log.info("处理组模板发送: requestId={}, groupCode={}", request.getRequestId(), request.getGroupCode());

        SendNotificationResponse response = new SendNotificationResponse();
        response.setRequestId(request.getRequestId());
        response.setProcessedAt(LocalDateTime.now());
        response.setResults(new ArrayList<>());

        try {
            // 获取组成员
            List<RecipientGroupMember> groupMembers = getGroupMembers(request.getGroupCode());
            if (groupMembers.isEmpty()) {
                response.setStatus("FAILED");
                response.setErrorMessage("组成员为空: " + request.getGroupCode());
                return response;
            }

            boolean allSuccess = true;

            // 为每个组成员发送
            for (RecipientGroupMember member : groupMembers) {
                // 检查该用户是否已处理过
                if (isRequestUserChannelProcessed(request.getRequestId(), member.getUserId(), template.getChannelCode())) {
                    log.info("用户模板请求已处理，跳过: requestId={}, userId={}, channelCode={}",
                            request.getRequestId(), member.getUserId(), template.getChannelCode());
                    continue;
                }

                // 创建组成员接收者信息
                NotificationSender.RecipientInfo memberRecipient = new NotificationSender.RecipientInfo();
                memberRecipient.setType("group");  // 直接设置为组类型
                memberRecipient.setUserId(member.getUserId());  // 语义明确的userId字段
                memberRecipient.setUserName(member.getUserName());
                memberRecipient.setEmail(member.getEmail());
                memberRecipient.setPhone(member.getPhone());
                memberRecipient.setImAccount(member.getImAccount());

                // 发送给组成员
                SendNotificationResponse.SendResult result = sendToRecipient(request, template, memberRecipient);
                response.getResults().add(result);

                if (!"SUCCESS".equals(result.getStatus())) {
                    allSuccess = false;
                }
            }

            response.setStatus(allSuccess ? "SUCCESS" : "PARTIAL_SUCCESS");
            return response;

        } catch (Exception e) {
            log.error("组模板发送异常: requestId={}", request.getRequestId(), e);
            response.setStatus("FAILED");
            response.setErrorMessage("组发送异常: " + e.getMessage());
            return response;
        }
    }

    /**
     * 处理多用户模板发送
     */
    private SendNotificationResponse handleMultiUserTemplateSend(SendNotificationRequest request, NotificationTemplate template) {
        log.info("处理多用户模板发送: requestId={}, userCount={}", request.getRequestId(), request.getUserCount());

        SendNotificationResponse response = new SendNotificationResponse();
        response.setRequestId(request.getRequestId());
        response.setProcessedAt(LocalDateTime.now());
        response.setResults(new ArrayList<>());

        try {
            boolean allSuccess = true;

            // 为每个用户发送
            for (BaseNotificationRequest.UserInfo user : request.getUsers()) {
                // 检查该用户是否已处理过
                if (isRequestUserChannelProcessed(request.getRequestId(), user.getUserId(), template.getChannelCode())) {
                    log.info("用户模板请求已处理，跳过: requestId={}, userId={}, channelCode={}",
                            request.getRequestId(), user.getUserId(), template.getChannelCode());
                    continue;
                }

                // 创建个人接收者信息
                NotificationSender.RecipientInfo recipient = new NotificationSender.RecipientInfo();
                recipient.setType("individual");
                recipient.setUserId(user.getUserId());  // 语义明确的userId字段
                recipient.setUserName(user.getUserName());
                recipient.setEmail(user.getEmail());
                recipient.setPhone(user.getPhone());
                recipient.setImAccount(user.getImAccount());

                // 发送给个人
                SendNotificationResponse.SendResult result = sendToRecipient(request, template, recipient);
                response.getResults().add(result);

                if (!"SUCCESS".equals(result.getStatus())) {
                    allSuccess = false;
                }
            }

            response.setStatus(allSuccess ? "SUCCESS" : "PARTIAL_SUCCESS");
            return response;

        } catch (Exception e) {
            log.error("多用户模板发送异常: requestId={}", request.getRequestId(), e);
            response.setStatus("FAILED");
            response.setErrorMessage("多用户发送异常: " + e.getMessage());
            return response;
        }
    }



    /**
     * 检查特定用户渠道的请求是否已处理（多用户多渠道幂等性）
     */
    private boolean isRequestUserChannelProcessed(String requestId, String userId, String channelCode) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getRequestId, requestId)
               .eq(Notification::getUserId, userId)
               .eq(Notification::getChannelCode, channelCode);
        return notificationMapper.selectCount(wrapper) > 0;
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
     * 获取组成员实体列表
     */
    private List<RecipientGroupMember> getGroupMembers(String groupCode) {
        LambdaQueryWrapper<RecipientGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipientGroupMember::getGroupCode, groupCode)
               .eq(RecipientGroupMember::getIsEnabled, true);

        return groupMemberMapper.selectList(wrapper);
    }

    /**
     * 获取组成员接收者信息列表（用于兼容旧代码）
     */
    private List<NotificationSender.RecipientInfo> getGroupMemberRecipients(String groupCode) {
        List<RecipientGroupMember> members = getGroupMembers(groupCode);
        List<NotificationSender.RecipientInfo> recipients = new ArrayList<>();

        for (RecipientGroupMember member : members) {
            NotificationSender.RecipientInfo recipient = new NotificationSender.RecipientInfo();
            recipient.setType("individual");
            recipient.setUserId(member.getUserId());  // 语义明确的userId字段
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
        notification.setRecipientType("group".equalsIgnoreCase(recipient.getType()) ?
                RecipientType.GROUP : RecipientType.INDIVIDUAL);

        // 设置接收者ID和用户ID
        if ("group".equalsIgnoreCase(recipient.getType())) {
            notification.setRecipientId(request.getGroupCode()); // 组发送时设置为组代码
            notification.setUserId(recipient.getUserId()); // 具体用户ID
        } else {
            notification.setRecipientId(recipient.getUserId()); // 个人发送时两者相同
            notification.setUserId(recipient.getUserId());
        }
        
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
        notification.setRecipientType("group".equalsIgnoreCase(recipient.getType()) ?
                RecipientType.GROUP : RecipientType.INDIVIDUAL);

        // 设置接收者ID和用户ID
        if ("group".equalsIgnoreCase(recipient.getType())) {
            notification.setRecipientId(request.getGroupCode()); // 组发送时设置为组代码
            notification.setUserId(recipient.getUserId()); // 具体用户ID
        } else {
            notification.setRecipientId(recipient.getUserId()); // 个人发送时两者相同
            notification.setUserId(recipient.getUserId());
        }

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
