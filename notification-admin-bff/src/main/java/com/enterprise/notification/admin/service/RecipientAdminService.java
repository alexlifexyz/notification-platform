package com.enterprise.notification.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.notification.admin.dto.*;
import com.enterprise.notification.admin.dto.common.PageResult;
import com.enterprise.notification.admin.dto.recipient.RecipientBatchImportRequest;
import com.enterprise.notification.admin.dto.recipient.RecipientCreateRequest;
import com.enterprise.notification.admin.dto.recipient.RecipientDto;
import com.enterprise.notification.admin.dto.recipient.RecipientUpdateRequest;
import com.enterprise.notification.admin.entity.RecipientGroup;
import com.enterprise.notification.admin.entity.RecipientGroupMember;
import com.enterprise.notification.admin.mapper.RecipientGroupMapper;
import com.enterprise.notification.admin.mapper.RecipientGroupMemberMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 收件人管理服务
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class RecipientAdminService {

    @Autowired
    private RecipientGroupMemberMapper memberMapper;

    @Autowired
    private RecipientGroupMapper groupMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 分页查询收件人
     */
    public PageResult<RecipientDto> getRecipients(long current, long size, String userId,
                                                  String userName, String phone, String email,
                                                  String groupCode, Boolean isEnabled) {
        Page<RecipientGroupMember> page = new Page<>(current, size);
        
        LambdaQueryWrapper<RecipientGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(userId), RecipientGroupMember::getUserId, userId)
               .like(StringUtils.hasText(userName), RecipientGroupMember::getUserName, userName)
               .like(StringUtils.hasText(phone), RecipientGroupMember::getPhone, phone)
               .like(StringUtils.hasText(email), RecipientGroupMember::getEmail, email)
               .eq(StringUtils.hasText(groupCode), RecipientGroupMember::getGroupCode, groupCode)
               .eq(isEnabled != null, RecipientGroupMember::getIsEnabled, isEnabled)
               .orderByDesc(RecipientGroupMember::getCreatedAt);

        IPage<RecipientGroupMember> result = memberMapper.selectPage(page, wrapper);
        
        List<RecipientDto> dtoList = result.getRecords().stream()
                .map(this::convertToRecipientDto)
                .collect(Collectors.toList());

        PageResult<RecipientDto> pageResult = new PageResult<>();
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setTotal(result.getTotal());
        pageResult.setPages(result.getPages());
        pageResult.setRecords(dtoList);

        return pageResult;
    }

    /**
     * 获取收件人详情
     */
    public RecipientDto getRecipient(String userId) {
        LambdaQueryWrapper<RecipientGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipientGroupMember::getUserId, userId);
        
        RecipientGroupMember member = memberMapper.selectOne(wrapper);
        if (member == null) {
            throw new RuntimeException("收件人不存在: " + userId);
        }

        return convertToRecipientDto(member);
    }

    /**
     * 创建收件人
     */
    @Transactional
    public RecipientDto createRecipient(RecipientCreateRequest request) {
        log.info("创建收件人: userId={}, userName={}, groupCode={}", 
                request.getUserId(), request.getUserName(), request.getGroupCode());

        // 检查用户ID是否已存在
        LambdaQueryWrapper<RecipientGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipientGroupMember::getUserId, request.getUserId());
        if (memberMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户ID已存在: " + request.getUserId());
        }

        // 检查组是否存在
        LambdaQueryWrapper<RecipientGroup> groupWrapper = new LambdaQueryWrapper<>();
        groupWrapper.eq(RecipientGroup::getGroupCode, request.getGroupCode());
        if (groupMapper.selectCount(groupWrapper) == 0) {
            throw new RuntimeException("收件人组不存在: " + request.getGroupCode());
        }

        RecipientGroupMember member = new RecipientGroupMember();
        BeanUtils.copyProperties(request, member);
        
        // 处理偏好渠道
        if (request.getPreferredChannels() != null) {
            try {
                member.setPreferredChannels(objectMapper.writeValueAsString(request.getPreferredChannels()));
            } catch (JsonProcessingException e) {
                log.error("序列化偏好渠道失败", e);
                throw new RuntimeException("偏好渠道格式错误");
            }
        }
        
        member.setCreatedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());

        memberMapper.insert(member);

        return convertToRecipientDto(member);
    }

    /**
     * 更新收件人
     */
    @Transactional
    public RecipientDto updateRecipient(String userId, RecipientUpdateRequest request) {
        log.info("更新收件人: userId={}, userName={}", userId, request.getUserName());

        LambdaQueryWrapper<RecipientGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipientGroupMember::getUserId, userId);
        
        RecipientGroupMember member = memberMapper.selectOne(wrapper);
        if (member == null) {
            throw new RuntimeException("收件人不存在: " + userId);
        }

        // 如果更换组，检查新组是否存在
        if (StringUtils.hasText(request.getGroupCode()) && 
            !request.getGroupCode().equals(member.getGroupCode())) {
            LambdaQueryWrapper<RecipientGroup> groupWrapper = new LambdaQueryWrapper<>();
            groupWrapper.eq(RecipientGroup::getGroupCode, request.getGroupCode());
            if (groupMapper.selectCount(groupWrapper) == 0) {
                throw new RuntimeException("收件人组不存在: " + request.getGroupCode());
            }
        }

        BeanUtils.copyProperties(request, member);
        
        // 处理偏好渠道
        if (request.getPreferredChannels() != null) {
            try {
                member.setPreferredChannels(objectMapper.writeValueAsString(request.getPreferredChannels()));
            } catch (JsonProcessingException e) {
                log.error("序列化偏好渠道失败", e);
                throw new RuntimeException("偏好渠道格式错误");
            }
        }
        
        member.setUpdatedAt(LocalDateTime.now());

        memberMapper.updateById(member);

        return convertToRecipientDto(member);
    }

    /**
     * 删除收件人
     */
    @Transactional
    public void deleteRecipient(String userId) {
        log.info("删除收件人: userId={}", userId);

        LambdaQueryWrapper<RecipientGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipientGroupMember::getUserId, userId);
        
        if (memberMapper.selectCount(wrapper) == 0) {
            throw new RuntimeException("收件人不存在: " + userId);
        }

        memberMapper.delete(wrapper);
    }

    /**
     * 批量导入收件人
     */
    @Transactional
    public List<RecipientDto> batchImportRecipients(RecipientBatchImportRequest request) {
        log.info("批量导入收件人: groupCode={}, count={}", 
                request.getGroupCode(), request.getRecipients().size());

        // 检查组是否存在
        LambdaQueryWrapper<RecipientGroup> groupWrapper = new LambdaQueryWrapper<>();
        groupWrapper.eq(RecipientGroup::getGroupCode, request.getGroupCode());
        if (groupMapper.selectCount(groupWrapper) == 0) {
            throw new RuntimeException("收件人组不存在: " + request.getGroupCode());
        }

        List<RecipientDto> results = new ArrayList<>();
        
        for (RecipientBatchImportRequest.RecipientImportItem item : request.getRecipients()) {
            try {
                // 检查用户ID是否已存在
                LambdaQueryWrapper<RecipientGroupMember> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(RecipientGroupMember::getUserId, item.getUserId());
                if (memberMapper.selectCount(wrapper) > 0) {
                    log.warn("用户ID已存在，跳过: {}", item.getUserId());
                    continue;
                }

                RecipientGroupMember member = new RecipientGroupMember();
                member.setGroupCode(request.getGroupCode());
                member.setUserId(item.getUserId());
                member.setUserName(item.getUserName());
                member.setPhone(item.getPhone());
                member.setEmail(item.getEmail());
                member.setImAccount(item.getImAccount());
                
                // 处理偏好渠道
                if (item.getPreferredChannels() != null) {
                    try {
                        member.setPreferredChannels(objectMapper.writeValueAsString(item.getPreferredChannels()));
                    } catch (JsonProcessingException e) {
                        log.error("序列化偏好渠道失败: {}", item.getUserId(), e);
                        member.setPreferredChannels("[]");
                    }
                }
                
                member.setIsEnabled(true);
                member.setCreatedAt(LocalDateTime.now());
                member.setUpdatedAt(LocalDateTime.now());

                memberMapper.insert(member);
                results.add(convertToRecipientDto(member));
                
            } catch (Exception e) {
                log.error("导入收件人失败: {}", item.getUserId(), e);
            }
        }

        return results;
    }

    /**
     * 转换为RecipientDto
     */
    private RecipientDto convertToRecipientDto(RecipientGroupMember member) {
        RecipientDto dto = new RecipientDto();
        BeanUtils.copyProperties(member, dto);
        
        // 获取组名称
        LambdaQueryWrapper<RecipientGroup> groupWrapper = new LambdaQueryWrapper<>();
        groupWrapper.eq(RecipientGroup::getGroupCode, member.getGroupCode());
        RecipientGroup group = groupMapper.selectOne(groupWrapper);
        if (group != null) {
            dto.setGroupName(group.getGroupName());
        }
        
        // 处理偏好渠道
        if (StringUtils.hasText(member.getPreferredChannels())) {
            try {
                List<String> channels = objectMapper.readValue(member.getPreferredChannels(), List.class);
                dto.setPreferredChannels(channels);
            } catch (JsonProcessingException e) {
                log.error("反序列化偏好渠道失败", e);
                dto.setPreferredChannels(new ArrayList<>());
            }
        } else {
            dto.setPreferredChannels(new ArrayList<>());
        }
        
        return dto;
    }
}
