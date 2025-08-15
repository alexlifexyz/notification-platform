package com.enterprise.notification.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.notification.admin.dto.common.PageResult;
import com.enterprise.notification.admin.dto.notification.NotificationRecordDto;
import com.enterprise.notification.admin.dto.notification.NotificationResendRequest;
import com.enterprise.notification.admin.dto.notification.NotificationStatisticsDto;
import com.enterprise.notification.admin.entity.Notification;
import com.enterprise.notification.admin.entity.NotificationChannel;
import com.enterprise.notification.admin.entity.NotificationTemplate;
import com.enterprise.notification.admin.mapper.NotificationChannelMapper;
import com.enterprise.notification.admin.mapper.NotificationMapper;
import com.enterprise.notification.admin.mapper.NotificationTemplateMapper;
import com.enterprise.notification.client.NotificationClient;
import com.enterprise.notification.common.dto.BaseNotificationRequest;
import com.enterprise.notification.common.dto.SendNotificationRequest;
import com.enterprise.notification.common.dto.SendNotificationResponse;
import com.enterprise.notification.enums.RecipientType;
import com.enterprise.notification.enums.SendStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 通知审计监控服务
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class NotificationAuditService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private NotificationTemplateMapper templateMapper;

    @Autowired
    private NotificationChannelMapper channelMapper;

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 分页查询通知记录
     */
    public PageResult<NotificationRecordDto> getNotificationRecords(long current, long size,
                                                                    String requestId, String templateCode,
                                                                    String recipientId, String status,
                                                                    LocalDateTime startTime, LocalDateTime endTime) {
        Page<Notification> page = new Page<>(current, size);
        
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(requestId), Notification::getRequestId, requestId)
               .like(StringUtils.hasText(templateCode), Notification::getTemplateCode, templateCode)
               .like(StringUtils.hasText(recipientId), Notification::getRecipientId, recipientId)
               .eq(StringUtils.hasText(status), Notification::getSendStatus, 
                   StringUtils.hasText(status) ? SendStatus.valueOf(status) : null)
               .ge(startTime != null, Notification::getCreatedAt, startTime)
               .le(endTime != null, Notification::getCreatedAt, endTime)
               .orderByDesc(Notification::getCreatedAt);

        IPage<Notification> result = notificationMapper.selectPage(page, wrapper);

        // 获取模板和渠道信息映射
        Map<String, String> templateNameMap = getTemplateNameMap();
        Map<String, String> channelNameMap = getChannelNameMap();

        List<NotificationRecordDto> recordDtos = result.getRecords().stream()
                .map(notification -> convertToRecordDto(notification, templateNameMap, channelNameMap))
                .collect(Collectors.toList());

        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), recordDtos);
    }

    /**
     * 获取通知记录详情
     */
    public NotificationRecordDto getNotificationRecord(Long id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new RuntimeException("通知记录不存在: " + id);
        }

        Map<String, String> templateNameMap = getTemplateNameMap();
        Map<String, String> channelNameMap = getChannelNameMap();

        return convertToRecordDto(notification, templateNameMap, channelNameMap);
    }

    /**
     * 重发失败通知
     */
    public SendNotificationResponse resendNotification(Long id, NotificationResendRequest request) {
        log.info("重发通知: id={}, reason={}", id, request.getReason());

        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new RuntimeException("通知记录不存在: " + id);
        }

        if (notification.getSendStatus() == SendStatus.SUCCESS) {
            throw new RuntimeException("成功的通知不能重发");
        }

        try {
            // 构建重发请求
            SendNotificationRequest sendRequest = new SendNotificationRequest();
            sendRequest.setRequestId(notification.getRequestId() + "_resend_" + System.currentTimeMillis());
            sendRequest.setTemplateCode(notification.getTemplateCode());

            // 解析接收者信息
            if (StringUtils.hasText(notification.getRecipientInfo())) {
                Map<String, Object> recipientInfoMap = objectMapper.readValue(
                        notification.getRecipientInfo(), new TypeReference<Map<String, Object>>() {});
                
                // 根据接收者类型设置请求
                if (notification.getRecipientType() == RecipientType.GROUP) {
                    // 组发送
                    sendRequest.setGroupCode(notification.getRecipientId());
                } else {
                    // 个人发送 - 使用新的UserInfo结构
                    BaseNotificationRequest.UserInfo user = new BaseNotificationRequest.UserInfo();
                    user.setUserId(notification.getRecipientId());
                    user.setUserName((String) recipientInfoMap.get("userName"));
                    user.setPhone((String) recipientInfoMap.get("phone"));
                    user.setEmail((String) recipientInfoMap.get("email"));
                    user.setImAccount((String) recipientInfoMap.get("imAccount"));

                    sendRequest.setUsers(Arrays.asList(user));
                }
            }

            // 解析模板参数
            if (StringUtils.hasText(notification.getTemplateParams())) {
                Map<String, Object> templateParams = objectMapper.readValue(
                        notification.getTemplateParams(), new TypeReference<Map<String, Object>>() {});
                sendRequest.setTemplateParams(templateParams);
            }

            // 调用通知服务重发
            SendNotificationResponse response = notificationClient.sendNotification(sendRequest);

            log.info("通知重发完成: originalId={}, newRequestId={}, status={}", 
                    id, sendRequest.getRequestId(), response.getStatus());

            return response;

        } catch (Exception e) {
            log.error("通知重发失败: id={}", id, e);
            throw new RuntimeException("重发失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取通知统计
     */
    public NotificationStatisticsDto getNotificationStatistics(LocalDateTime startTime, LocalDateTime endTime,
                                                               String groupBy) {
        log.info("获取通知统计: startTime={}, endTime={}, groupBy={}", startTime, endTime, groupBy);

        NotificationStatisticsDto statistics = new NotificationStatisticsDto();
        statistics.setStartTime(startTime);
        statistics.setEndTime(endTime);

        // 基础统计查询
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(startTime != null, Notification::getCreatedAt, startTime)
               .le(endTime != null, Notification::getCreatedAt, endTime);

        List<Notification> notifications = notificationMapper.selectList(wrapper);

        // 总体统计
        statistics.setTotalCount((long) notifications.size());
        statistics.setSuccessCount(notifications.stream()
                .mapToLong(n -> n.getSendStatus() == SendStatus.SUCCESS ? 1 : 0).sum());
        statistics.setFailedCount(notifications.stream()
                .mapToLong(n -> n.getSendStatus() == SendStatus.FAILED ? 1 : 0).sum());
        
        if (statistics.getTotalCount() > 0) {
            statistics.setSuccessRate((double) statistics.getSuccessCount() / statistics.getTotalCount() * 100);
        } else {
            statistics.setSuccessRate(0.0);
        }

        // 按渠道统计
        statistics.setChannelStatistics(getChannelStatistics(notifications));

        // 按模板统计
        statistics.setTemplateStatistics(getTemplateStatistics(notifications));

        // 时间序列统计
        if ("hour".equals(groupBy)) {
            statistics.setTimeSeriesStatistics(getHourlyStatistics(notifications, startTime, endTime));
        } else if ("day".equals(groupBy)) {
            statistics.setTimeSeriesStatistics(getDailyStatistics(notifications, startTime, endTime));
        }

        return statistics;
    }

    /**
     * 按渠道统计
     */
    private List<NotificationStatisticsDto.ChannelStatistics> getChannelStatistics(List<Notification> notifications) {
        Map<String, String> channelNameMap = getChannelNameMap();
        
        return notifications.stream()
                .collect(Collectors.groupingBy(Notification::getChannelCode))
                .entrySet().stream()
                .map(entry -> {
                    String channelCode = entry.getKey();
                    List<Notification> channelNotifications = entry.getValue();
                    
                    NotificationStatisticsDto.ChannelStatistics stat = 
                            new NotificationStatisticsDto.ChannelStatistics();
                    stat.setChannelCode(channelCode);
                    stat.setChannelName(channelNameMap.get(channelCode));
                    stat.setCount((long) channelNotifications.size());
                    stat.setSuccessCount(channelNotifications.stream()
                            .mapToLong(n -> n.getSendStatus() == SendStatus.SUCCESS ? 1 : 0).sum());
                    stat.setFailedCount(channelNotifications.stream()
                            .mapToLong(n -> n.getSendStatus() == SendStatus.FAILED ? 1 : 0).sum());
                    
                    if (stat.getCount() > 0) {
                        stat.setSuccessRate((double) stat.getSuccessCount() / stat.getCount() * 100);
                    } else {
                        stat.setSuccessRate(0.0);
                    }
                    
                    return stat;
                })
                .collect(Collectors.toList());
    }

    /**
     * 按模板统计
     */
    private List<NotificationStatisticsDto.TemplateStatistics> getTemplateStatistics(List<Notification> notifications) {
        Map<String, String> templateNameMap = getTemplateNameMap();
        
        return notifications.stream()
                .collect(Collectors.groupingBy(Notification::getTemplateCode))
                .entrySet().stream()
                .map(entry -> {
                    String templateCode = entry.getKey();
                    List<Notification> templateNotifications = entry.getValue();
                    
                    NotificationStatisticsDto.TemplateStatistics stat = 
                            new NotificationStatisticsDto.TemplateStatistics();
                    stat.setTemplateCode(templateCode);
                    stat.setTemplateName(templateNameMap.get(templateCode));
                    stat.setCount((long) templateNotifications.size());
                    stat.setSuccessCount(templateNotifications.stream()
                            .mapToLong(n -> n.getSendStatus() == SendStatus.SUCCESS ? 1 : 0).sum());
                    stat.setFailedCount(templateNotifications.stream()
                            .mapToLong(n -> n.getSendStatus() == SendStatus.FAILED ? 1 : 0).sum());
                    
                    if (stat.getCount() > 0) {
                        stat.setSuccessRate((double) stat.getSuccessCount() / stat.getCount() * 100);
                    } else {
                        stat.setSuccessRate(0.0);
                    }
                    
                    return stat;
                })
                .collect(Collectors.toList());
    }

    /**
     * 按小时统计
     */
    private List<NotificationStatisticsDto.TimeSeriesStatistics> getHourlyStatistics(
            List<Notification> notifications, LocalDateTime startTime, LocalDateTime endTime) {
        
        if (startTime == null) startTime = LocalDateTime.now().minusDays(1);
        if (endTime == null) endTime = LocalDateTime.now();
        
        Map<LocalDateTime, List<Notification>> hourlyGroups = notifications.stream()
                .collect(Collectors.groupingBy(n -> n.getCreatedAt().truncatedTo(ChronoUnit.HOURS)));
        
        List<NotificationStatisticsDto.TimeSeriesStatistics> result = new ArrayList<>();
        LocalDateTime current = startTime.truncatedTo(ChronoUnit.HOURS);
        
        while (!current.isAfter(endTime)) {
            List<Notification> hourNotifications = hourlyGroups.getOrDefault(current, new ArrayList<>());
            
            NotificationStatisticsDto.TimeSeriesStatistics stat = 
                    new NotificationStatisticsDto.TimeSeriesStatistics();
            stat.setTime(current);
            stat.setCount((long) hourNotifications.size());
            stat.setSuccessCount(hourNotifications.stream()
                    .mapToLong(n -> n.getSendStatus() == SendStatus.SUCCESS ? 1 : 0).sum());
            stat.setFailedCount(hourNotifications.stream()
                    .mapToLong(n -> n.getSendStatus() == SendStatus.FAILED ? 1 : 0).sum());
            
            result.add(stat);
            current = current.plusHours(1);
        }
        
        return result;
    }

    /**
     * 按天统计
     */
    private List<NotificationStatisticsDto.TimeSeriesStatistics> getDailyStatistics(
            List<Notification> notifications, LocalDateTime startTime, LocalDateTime endTime) {
        
        if (startTime == null) startTime = LocalDateTime.now().minusDays(7);
        if (endTime == null) endTime = LocalDateTime.now();
        
        Map<LocalDateTime, List<Notification>> dailyGroups = notifications.stream()
                .collect(Collectors.groupingBy(n -> n.getCreatedAt().truncatedTo(ChronoUnit.DAYS)));
        
        List<NotificationStatisticsDto.TimeSeriesStatistics> result = new ArrayList<>();
        LocalDateTime current = startTime.truncatedTo(ChronoUnit.DAYS);
        
        while (!current.isAfter(endTime)) {
            List<Notification> dayNotifications = dailyGroups.getOrDefault(current, new ArrayList<>());
            
            NotificationStatisticsDto.TimeSeriesStatistics stat = 
                    new NotificationStatisticsDto.TimeSeriesStatistics();
            stat.setTime(current);
            stat.setCount((long) dayNotifications.size());
            stat.setSuccessCount(dayNotifications.stream()
                    .mapToLong(n -> n.getSendStatus() == SendStatus.SUCCESS ? 1 : 0).sum());
            stat.setFailedCount(dayNotifications.stream()
                    .mapToLong(n -> n.getSendStatus() == SendStatus.FAILED ? 1 : 0).sum());
            
            result.add(stat);
            current = current.plusDays(1);
        }
        
        return result;
    }

    /**
     * 转换为记录DTO
     */
    private NotificationRecordDto convertToRecordDto(Notification notification, 
                                                   Map<String, String> templateNameMap,
                                                   Map<String, String> channelNameMap) {
        NotificationRecordDto dto = new NotificationRecordDto();
        BeanUtils.copyProperties(notification, dto);
        
        dto.setRecipientType(notification.getRecipientType().name());
        dto.setSendStatus(notification.getSendStatus().name());
        dto.setTemplateName(templateNameMap.get(notification.getTemplateCode()));
        dto.setChannelName(channelNameMap.get(notification.getChannelCode()));
        
        // 解析JSON字段
        try {
            if (StringUtils.hasText(notification.getRecipientInfo())) {
                dto.setRecipientInfo(objectMapper.readValue(notification.getRecipientInfo(), 
                        new TypeReference<Map<String, Object>>() {}));
            }
            if (StringUtils.hasText(notification.getTemplateParams())) {
                dto.setTemplateParams(objectMapper.readValue(notification.getTemplateParams(), 
                        new TypeReference<Map<String, Object>>() {}));
            }
        } catch (JsonProcessingException e) {
            log.warn("JSON解析失败", e);
        }
        
        return dto;
    }

    /**
     * 获取模板名称映射
     */
    private Map<String, String> getTemplateNameMap() {
        List<NotificationTemplate> templates = templateMapper.selectList(null);
        return templates.stream()
                .collect(Collectors.toMap(
                        NotificationTemplate::getTemplateCode,
                        NotificationTemplate::getTemplateName,
                        (existing, replacement) -> existing
                ));
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
