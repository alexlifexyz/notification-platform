package com.enterprise.notification.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.notification.admin.dto.common.PageResult;
import com.enterprise.notification.admin.dto.message.InAppMessageBatchOperationRequest;
import com.enterprise.notification.admin.dto.message.InAppMessageDto;
import com.enterprise.notification.admin.dto.message.InAppMessageStatisticsDto;
import com.enterprise.notification.admin.entity.UserInAppMessage;
import com.enterprise.notification.admin.mapper.UserInAppMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 站内信管理服务
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class InAppMessageAdminService {

    @Autowired
    private UserInAppMessageMapper messageMapper;

    /**
     * 分页查询站内信
     */
    public PageResult<InAppMessageDto> getInAppMessages(long current, long size, String userId,
                                                        String subject, Boolean isRead,
                                                        LocalDateTime startTime, LocalDateTime endTime) {
        Page<UserInAppMessage> page = new Page<>(current, size);
        
        LambdaQueryWrapper<UserInAppMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(userId), UserInAppMessage::getUserId, userId)
               .like(StringUtils.hasText(subject), UserInAppMessage::getSubject, subject)
               .eq(isRead != null, UserInAppMessage::getIsRead, isRead)
               .ge(startTime != null, UserInAppMessage::getCreatedAt, startTime)
               .le(endTime != null, UserInAppMessage::getCreatedAt, endTime)
               .orderByDesc(UserInAppMessage::getCreatedAt);

        IPage<UserInAppMessage> result = messageMapper.selectPage(page, wrapper);
        
        List<InAppMessageDto> dtoList = result.getRecords().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        PageResult<InAppMessageDto> pageResult = new PageResult<>();
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setTotal(result.getTotal());
        pageResult.setPages(result.getPages());
        pageResult.setRecords(dtoList);

        return pageResult;
    }

    /**
     * 获取站内信详情
     */
    public InAppMessageDto getInAppMessage(Long id) {
        UserInAppMessage message = messageMapper.selectById(id);
        if (message == null) {
            throw new RuntimeException("站内信不存在: " + id);
        }

        return convertToDto(message);
    }

    /**
     * 标记已读
     */
    @Transactional
    public void markAsRead(Long id) {
        log.info("标记站内信已读: id={}", id);

        UserInAppMessage message = messageMapper.selectById(id);
        if (message == null) {
            throw new RuntimeException("站内信不存在: " + id);
        }

        if (!message.getIsRead()) {
            message.setIsRead(true);
            message.setReadAt(LocalDateTime.now());
            message.setUpdatedAt(LocalDateTime.now());
            messageMapper.updateById(message);
        }
    }

    /**
     * 标记未读
     */
    @Transactional
    public void markAsUnread(Long id) {
        log.info("标记站内信未读: id={}", id);

        UserInAppMessage message = messageMapper.selectById(id);
        if (message == null) {
            throw new RuntimeException("站内信不存在: " + id);
        }

        if (message.getIsRead()) {
            message.setIsRead(false);
            message.setReadAt(null);
            message.setUpdatedAt(LocalDateTime.now());
            messageMapper.updateById(message);
        }
    }

    /**
     * 删除站内信
     */
    @Transactional
    public void deleteInAppMessage(Long id) {
        log.info("删除站内信: id={}", id);

        if (messageMapper.selectById(id) == null) {
            throw new RuntimeException("站内信不存在: " + id);
        }

        messageMapper.deleteById(id);
    }

    /**
     * 批量操作
     */
    @Transactional
    public void batchOperation(InAppMessageBatchOperationRequest request) {
        log.info("批量操作站内信: operation={}, count={}", 
                request.getOperation(), request.getMessageIds().size());

        for (Long messageId : request.getMessageIds()) {
            try {
                switch (request.getOperation()) {
                    case MARK_READ:
                        markAsRead(messageId);
                        break;
                    case MARK_UNREAD:
                        markAsUnread(messageId);
                        break;
                    case DELETE:
                        deleteInAppMessage(messageId);
                        break;
                    default:
                        log.warn("未知的操作类型: {}", request.getOperation());
                }
            } catch (Exception e) {
                log.error("批量操作失败: messageId={}", messageId, e);
            }
        }
    }

    /**
     * 获取站内信统计
     */
    public InAppMessageStatisticsDto getStatistics(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("获取站内信统计: userId={}, startTime={}, endTime={}", userId, startTime, endTime);

        // 获取总体统计
        Map<String, Object> stats = messageMapper.getStatistics(userId, startTime, endTime);
        
        InAppMessageStatisticsDto dto = new InAppMessageStatisticsDto();
        dto.setTotalCount(((Number) stats.get("totalCount")).longValue());
        dto.setUnreadCount(((Number) stats.get("unreadCount")).longValue());
        dto.setReadCount(((Number) stats.get("readCount")).longValue());
        
        // 计算已读率
        if (dto.getTotalCount() > 0) {
            dto.setReadRate(dto.getReadCount() * 100.0 / dto.getTotalCount());
        } else {
            dto.setReadRate(0.0);
        }

        // 获取每日统计
        List<Map<String, Object>> dailyStats = messageMapper.getDailyStatistics(userId, startTime, endTime);
        List<InAppMessageStatisticsDto.DailyStatistics> dailyList = new ArrayList<>();
        
        for (Map<String, Object> dailyStat : dailyStats) {
            InAppMessageStatisticsDto.DailyStatistics daily = new InAppMessageStatisticsDto.DailyStatistics();
            daily.setDate(dailyStat.get("date").toString());
            daily.setTotalCount(((Number) dailyStat.get("totalCount")).longValue());
            daily.setUnreadCount(((Number) dailyStat.get("unreadCount")).longValue());
            daily.setReadCount(((Number) dailyStat.get("readCount")).longValue());
            dailyList.add(daily);
        }
        
        dto.setDailyStatistics(dailyList);

        return dto;
    }

    /**
     * 转换为DTO
     */
    private InAppMessageDto convertToDto(UserInAppMessage message) {
        InAppMessageDto dto = new InAppMessageDto();
        BeanUtils.copyProperties(message, dto);
        return dto;
    }
}
