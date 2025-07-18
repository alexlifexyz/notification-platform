package com.enterprise.notification.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.notification.admin.dto.*;
import com.enterprise.notification.admin.entity.NotificationChannel;
import com.enterprise.notification.admin.entity.NotificationTemplate;
import com.enterprise.notification.admin.mapper.NotificationChannelMapper;
import com.enterprise.notification.admin.mapper.NotificationTemplateMapper;
import com.enterprise.notification.client.NotificationClient;
import com.enterprise.notification.common.dto.SendNotificationRequest;
import com.enterprise.notification.common.dto.SendNotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 模板管理服务
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class TemplateAdminService {

    @Autowired
    private NotificationTemplateMapper templateMapper;

    @Autowired
    private NotificationChannelMapper channelMapper;

    @Autowired
    private NotificationClient notificationClient;

    /**
     * 分页查询模板列表
     */
    public PageResult<TemplateDto> getTemplates(long current, long size, String templateCode, 
                                               String templateName, String channelCode, Boolean isEnabled) {
        Page<NotificationTemplate> page = new Page<>(current, size);
        
        LambdaQueryWrapper<NotificationTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(templateCode), NotificationTemplate::getTemplateCode, templateCode)
               .like(StringUtils.hasText(templateName), NotificationTemplate::getTemplateName, templateName)
               .eq(StringUtils.hasText(channelCode), NotificationTemplate::getChannelCode, channelCode)
               .eq(isEnabled != null, NotificationTemplate::getIsEnabled, isEnabled)
               .orderByDesc(NotificationTemplate::getCreatedAt);

        IPage<NotificationTemplate> result = templateMapper.selectPage(page, wrapper);

        // 获取渠道信息映射
        Map<String, String> channelNameMap = getChannelNameMap();

        List<TemplateDto> templateDtos = result.getRecords().stream()
                .map(template -> {
                    TemplateDto dto = new TemplateDto();
                    BeanUtils.copyProperties(template, dto);
                    dto.setChannelName(channelNameMap.get(template.getChannelCode()));
                    return dto;
                })
                .collect(Collectors.toList());

        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), templateDtos);
    }

    /**
     * 获取单个模板详情
     */
    public TemplateDto getTemplate(Long id) {
        NotificationTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new RuntimeException("模板不存在: " + id);
        }

        TemplateDto dto = new TemplateDto();
        BeanUtils.copyProperties(template, dto);
        
        // 设置渠道名称
        Map<String, String> channelNameMap = getChannelNameMap();
        dto.setChannelName(channelNameMap.get(template.getChannelCode()));

        return dto;
    }

    /**
     * 创建模板
     */
    @Transactional
    public TemplateDto createTemplate(TemplateCreateRequest request) {
        log.info("创建模板: templateCode={}, templateName={}", request.getTemplateCode(), request.getTemplateName());

        // 检查模板代码是否已存在
        LambdaQueryWrapper<NotificationTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationTemplate::getTemplateCode, request.getTemplateCode());
        if (templateMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("模板代码已存在: " + request.getTemplateCode());
        }

        // 验证渠道代码
        validateChannelCode(request.getChannelCode());

        NotificationTemplate template = new NotificationTemplate();
        BeanUtils.copyProperties(request, template);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());

        templateMapper.insert(template);

        log.info("模板创建成功: id={}, templateCode={}", template.getId(), template.getTemplateCode());

        return getTemplate(template.getId());
    }

    /**
     * 更新模板
     */
    @Transactional
    public TemplateDto updateTemplate(Long id, TemplateUpdateRequest request) {
        log.info("更新模板: id={}, templateName={}", id, request.getTemplateName());

        NotificationTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new RuntimeException("模板不存在: " + id);
        }

        // 验证渠道代码
        validateChannelCode(request.getChannelCode());

        BeanUtils.copyProperties(request, template);
        template.setUpdatedAt(LocalDateTime.now());

        templateMapper.updateById(template);

        log.info("模板更新成功: id={}, templateCode={}", template.getId(), template.getTemplateCode());

        return getTemplate(id);
    }

    /**
     * 删除模板
     */
    @Transactional
    public void deleteTemplate(Long id) {
        log.info("删除模板: id={}", id);

        NotificationTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new RuntimeException("模板不存在: " + id);
        }

        templateMapper.deleteById(id);

        log.info("模板删除成功: id={}, templateCode={}", id, template.getTemplateCode());
    }

    /**
     * 测试发送模板
     */
    public SendNotificationResponse testSendTemplate(TemplateTestSendRequest request) {
        log.info("测试发送模板: templateCode={}, userId={}", 
                request.getTemplateCode(), request.getTestRecipient().getUserId());

        try {
            // 构建发送请求
            SendNotificationRequest sendRequest = new SendNotificationRequest();
            sendRequest.setRequestId("test_" + UUID.randomUUID().toString().replace("-", ""));
            sendRequest.setTemplateCode(request.getTemplateCode());
            sendRequest.setTemplateParams(request.getTemplateParams());

            // 设置接收者信息
            SendNotificationRequest.RecipientInfo recipient = new SendNotificationRequest.RecipientInfo();
            recipient.setType("individual");
            recipient.setId(request.getTestRecipient().getUserId());

            SendNotificationRequest.ContactInfo contactInfo = new SendNotificationRequest.ContactInfo();
            contactInfo.setUserName(request.getTestRecipient().getUserName());
            contactInfo.setPhone(request.getTestRecipient().getPhone());
            contactInfo.setEmail(request.getTestRecipient().getEmail());
            contactInfo.setImAccount(request.getTestRecipient().getImAccount());
            recipient.setContactInfo(contactInfo);

            sendRequest.setRecipient(recipient);

            // 调用通知服务发送
            SendNotificationResponse response = notificationClient.sendNotification(sendRequest);

            log.info("模板测试发送完成: templateCode={}, status={}", 
                    request.getTemplateCode(), response.getStatus());

            return response;

        } catch (Exception e) {
            log.error("模板测试发送失败: templateCode={}", request.getTemplateCode(), e);
            throw new RuntimeException("测试发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证渠道代码
     */
    private void validateChannelCode(String channelCode) {
        LambdaQueryWrapper<NotificationChannel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationChannel::getChannelCode, channelCode)
               .eq(NotificationChannel::getIsEnabled, true);
        
        if (channelMapper.selectCount(wrapper) == 0) {
            throw new RuntimeException("无效的渠道代码: " + channelCode);
        }
    }

    /**
     * 获取渠道名称映射
     */
    private Map<String, String> getChannelNameMap() {
        List<NotificationChannel> channels = channelMapper.selectList(null);
        return channels.stream()
                .collect(Collectors.toMap(
                        NotificationChannel::getChannelCode,
                        NotificationChannel::getChannelName,
                        (existing, replacement) -> existing
                ));
    }
}
