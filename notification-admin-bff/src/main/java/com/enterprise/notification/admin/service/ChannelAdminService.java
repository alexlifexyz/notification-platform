package com.enterprise.notification.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.notification.admin.dto.channel.ChannelCreateRequest;
import com.enterprise.notification.admin.dto.channel.ChannelDto;
import com.enterprise.notification.admin.dto.channel.ChannelUpdateRequest;
import com.enterprise.notification.admin.entity.NotificationChannel;
import com.enterprise.notification.admin.mapper.NotificationChannelMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 渠道管理服务
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class ChannelAdminService {

    @Autowired
    private NotificationChannelMapper channelMapper;

    /**
     * 查询所有渠道
     */
    public List<ChannelDto> getAllChannels() {
        LambdaQueryWrapper<NotificationChannel> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(NotificationChannel::getChannelCode);

        List<NotificationChannel> channels = channelMapper.selectList(wrapper);
        
        return channels.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取渠道详情
     */
    public ChannelDto getChannel(String channelCode) {
        LambdaQueryWrapper<NotificationChannel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationChannel::getChannelCode, channelCode);
        
        NotificationChannel channel = channelMapper.selectOne(wrapper);
        if (channel == null) {
            throw new RuntimeException("渠道不存在: " + channelCode);
        }

        return convertToDto(channel);
    }

    /**
     * 创建渠道
     */
    @Transactional
    public ChannelDto createChannel(ChannelCreateRequest request) {
        log.info("创建渠道: channelCode={}, channelName={}", 
                request.getChannelCode(), request.getChannelName());

        // 检查渠道代码是否已存在
        LambdaQueryWrapper<NotificationChannel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationChannel::getChannelCode, request.getChannelCode());
        if (channelMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("渠道代码已存在: " + request.getChannelCode());
        }

        NotificationChannel channel = new NotificationChannel();
        BeanUtils.copyProperties(request, channel);
        channel.setCreatedAt(LocalDateTime.now());
        channel.setUpdatedAt(LocalDateTime.now());

        channelMapper.insert(channel);

        return convertToDto(channel);
    }

    /**
     * 更新渠道
     */
    @Transactional
    public ChannelDto updateChannel(String channelCode, ChannelUpdateRequest request) {
        log.info("更新渠道: channelCode={}, channelName={}", channelCode, request.getChannelName());

        LambdaQueryWrapper<NotificationChannel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationChannel::getChannelCode, channelCode);
        
        NotificationChannel channel = channelMapper.selectOne(wrapper);
        if (channel == null) {
            throw new RuntimeException("渠道不存在: " + channelCode);
        }

        BeanUtils.copyProperties(request, channel);
        channel.setUpdatedAt(LocalDateTime.now());

        channelMapper.updateById(channel);

        return convertToDto(channel);
    }

    /**
     * 切换渠道状态
     */
    @Transactional
    public ChannelDto toggleChannelStatus(String channelCode) {
        log.info("切换渠道状态: channelCode={}", channelCode);

        LambdaQueryWrapper<NotificationChannel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationChannel::getChannelCode, channelCode);
        
        NotificationChannel channel = channelMapper.selectOne(wrapper);
        if (channel == null) {
            throw new RuntimeException("渠道不存在: " + channelCode);
        }

        channel.setIsEnabled(!channel.getIsEnabled());
        channel.setUpdatedAt(LocalDateTime.now());

        channelMapper.updateById(channel);

        return convertToDto(channel);
    }

    /**
     * 删除渠道
     */
    @Transactional
    public void deleteChannel(String channelCode) {
        log.info("删除渠道: channelCode={}", channelCode);

        LambdaQueryWrapper<NotificationChannel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationChannel::getChannelCode, channelCode);
        
        if (channelMapper.selectCount(wrapper) == 0) {
            throw new RuntimeException("渠道不存在: " + channelCode);
        }

        channelMapper.delete(wrapper);
    }

    /**
     * 转换为DTO
     */
    private ChannelDto convertToDto(NotificationChannel channel) {
        ChannelDto dto = new ChannelDto();
        BeanUtils.copyProperties(channel, dto);
        return dto;
    }
}
