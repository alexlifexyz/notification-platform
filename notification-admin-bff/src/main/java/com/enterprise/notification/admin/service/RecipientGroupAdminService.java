package com.enterprise.notification.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.notification.admin.dto.*;
import com.enterprise.notification.admin.entity.RecipientGroup;
import com.enterprise.notification.admin.entity.RecipientGroupMember;
import com.enterprise.notification.admin.mapper.RecipientGroupMapper;
import com.enterprise.notification.admin.mapper.RecipientGroupMemberMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
 * 收件人组管理服务
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class RecipientGroupAdminService {

    @Autowired
    private RecipientGroupMapper groupMapper;

    @Autowired
    private RecipientGroupMemberMapper memberMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 分页查询收件人组
     */
    public PageResult<RecipientGroupDto> getRecipientGroups(long current, long size, 
                                                           String groupCode, String groupName, Boolean isEnabled) {
        Page<RecipientGroup> page = new Page<>(current, size);
        
        LambdaQueryWrapper<RecipientGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(groupCode), RecipientGroup::getGroupCode, groupCode)
               .like(StringUtils.hasText(groupName), RecipientGroup::getGroupName, groupName)
               .eq(isEnabled != null, RecipientGroup::getIsEnabled, isEnabled)
               .orderByDesc(RecipientGroup::getCreatedAt);

        IPage<RecipientGroup> result = groupMapper.selectPage(page, wrapper);

        List<RecipientGroupDto> groupDtos = result.getRecords().stream()
                .map(group -> {
                    RecipientGroupDto dto = new RecipientGroupDto();
                    BeanUtils.copyProperties(group, dto);
                    
                    // 获取成员数量
                    LambdaQueryWrapper<RecipientGroupMember> memberWrapper = new LambdaQueryWrapper<>();
                    memberWrapper.eq(RecipientGroupMember::getGroupCode, group.getGroupCode())
                               .eq(RecipientGroupMember::getIsEnabled, true);
                    dto.setMemberCount(Math.toIntExact(memberMapper.selectCount(memberWrapper)));
                    
                    return dto;
                })
                .collect(Collectors.toList());

        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), groupDtos);
    }

    /**
     * 获取收件人组详情
     */
    public RecipientGroupDto getRecipientGroup(String groupCode) {
        LambdaQueryWrapper<RecipientGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipientGroup::getGroupCode, groupCode);
        
        RecipientGroup group = groupMapper.selectOne(wrapper);
        if (group == null) {
            throw new RuntimeException("收件人组不存在: " + groupCode);
        }

        RecipientGroupDto dto = new RecipientGroupDto();
        BeanUtils.copyProperties(group, dto);
        
        // 获取成员数量
        LambdaQueryWrapper<RecipientGroupMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(RecipientGroupMember::getGroupCode, groupCode)
                   .eq(RecipientGroupMember::getIsEnabled, true);
        dto.setMemberCount(Math.toIntExact(memberMapper.selectCount(memberWrapper)));

        return dto;
    }

    /**
     * 创建收件人组
     */
    @Transactional
    public RecipientGroupDto createRecipientGroup(RecipientGroupCreateRequest request) {
        log.info("创建收件人组: groupCode={}, groupName={}", request.getGroupCode(), request.getGroupName());

        // 检查组代码是否已存在
        LambdaQueryWrapper<RecipientGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipientGroup::getGroupCode, request.getGroupCode());
        if (groupMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("组代码已存在: " + request.getGroupCode());
        }

        RecipientGroup group = new RecipientGroup();
        BeanUtils.copyProperties(request, group);
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());

        groupMapper.insert(group);

        log.info("收件人组创建成功: id={}, groupCode={}", group.getId(), group.getGroupCode());

        return getRecipientGroup(group.getGroupCode());
    }

    /**
     * 更新收件人组
     */
    @Transactional
    public RecipientGroupDto updateRecipientGroup(String groupCode, RecipientGroupUpdateRequest request) {
        log.info("更新收件人组: groupCode={}, groupName={}", groupCode, request.getGroupName());

        LambdaQueryWrapper<RecipientGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipientGroup::getGroupCode, groupCode);
        
        RecipientGroup group = groupMapper.selectOne(wrapper);
        if (group == null) {
            throw new RuntimeException("收件人组不存在: " + groupCode);
        }

        BeanUtils.copyProperties(request, group);
        group.setUpdatedAt(LocalDateTime.now());

        groupMapper.updateById(group);

        log.info("收件人组更新成功: id={}, groupCode={}", group.getId(), group.getGroupCode());

        return getRecipientGroup(groupCode);
    }

    /**
     * 删除收件人组
     */
    @Transactional
    public void deleteRecipientGroup(String groupCode) {
        log.info("删除收件人组: groupCode={}", groupCode);

        LambdaQueryWrapper<RecipientGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipientGroup::getGroupCode, groupCode);
        
        RecipientGroup group = groupMapper.selectOne(wrapper);
        if (group == null) {
            throw new RuntimeException("收件人组不存在: " + groupCode);
        }

        // 删除组成员
        LambdaQueryWrapper<RecipientGroupMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(RecipientGroupMember::getGroupCode, groupCode);
        memberMapper.delete(memberWrapper);

        // 删除组
        groupMapper.deleteById(group.getId());

        log.info("收件人组删除成功: groupCode={}", groupCode);
    }

    /**
     * 分页查询组成员
     */
    public PageResult<RecipientGroupMemberDto> getGroupMembers(String groupCode, long current, long size) {
        Page<RecipientGroupMember> page = new Page<>(current, size);
        
        LambdaQueryWrapper<RecipientGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipientGroupMember::getGroupCode, groupCode)
               .orderByDesc(RecipientGroupMember::getCreatedAt);

        IPage<RecipientGroupMember> result = memberMapper.selectPage(page, wrapper);

        List<RecipientGroupMemberDto> memberDtos = result.getRecords().stream()
                .map(this::convertToMemberDto)
                .collect(Collectors.toList());

        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), memberDtos);
    }

    /**
     * 添加组成员
     */
    @Transactional
    public RecipientGroupMemberDto addGroupMember(String groupCode, RecipientGroupMemberCreateRequest request) {
        log.info("添加组成员: groupCode={}, userId={}", groupCode, request.getUserId());

        // 检查组是否存在
        LambdaQueryWrapper<RecipientGroup> groupWrapper = new LambdaQueryWrapper<>();
        groupWrapper.eq(RecipientGroup::getGroupCode, groupCode);
        if (groupMapper.selectCount(groupWrapper) == 0) {
            throw new RuntimeException("收件人组不存在: " + groupCode);
        }

        // 检查成员是否已存在
        LambdaQueryWrapper<RecipientGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipientGroupMember::getGroupCode, groupCode)
               .eq(RecipientGroupMember::getUserId, request.getUserId());
        if (memberMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户已在组中: " + request.getUserId());
        }

        RecipientGroupMember member = new RecipientGroupMember();
        BeanUtils.copyProperties(request, member);
        member.setGroupCode(groupCode);
        
        // 转换偏好渠道为JSON
        if (request.getPreferredChannels() != null) {
            try {
                member.setPreferredChannels(objectMapper.writeValueAsString(request.getPreferredChannels()));
            } catch (JsonProcessingException e) {
                log.warn("偏好渠道JSON序列化失败", e);
            }
        }
        
        member.setCreatedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());

        memberMapper.insert(member);

        log.info("组成员添加成功: id={}, groupCode={}, userId={}", member.getId(), groupCode, request.getUserId());

        return convertToMemberDto(member);
    }

    /**
     * 更新组成员
     */
    @Transactional
    public RecipientGroupMemberDto updateGroupMember(String groupCode, String userId, 
                                                    RecipientGroupMemberUpdateRequest request) {
        log.info("更新组成员: groupCode={}, userId={}", groupCode, userId);

        LambdaQueryWrapper<RecipientGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipientGroupMember::getGroupCode, groupCode)
               .eq(RecipientGroupMember::getUserId, userId);
        
        RecipientGroupMember member = memberMapper.selectOne(wrapper);
        if (member == null) {
            throw new RuntimeException("组成员不存在: " + userId);
        }

        BeanUtils.copyProperties(request, member);
        
        // 转换偏好渠道为JSON
        if (request.getPreferredChannels() != null) {
            try {
                member.setPreferredChannels(objectMapper.writeValueAsString(request.getPreferredChannels()));
            } catch (JsonProcessingException e) {
                log.warn("偏好渠道JSON序列化失败", e);
            }
        }
        
        member.setUpdatedAt(LocalDateTime.now());

        memberMapper.updateById(member);

        log.info("组成员更新成功: id={}, groupCode={}, userId={}", member.getId(), groupCode, userId);

        return convertToMemberDto(member);
    }

    /**
     * 删除组成员
     */
    @Transactional
    public void deleteGroupMember(String groupCode, String userId) {
        log.info("删除组成员: groupCode={}, userId={}", groupCode, userId);

        LambdaQueryWrapper<RecipientGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipientGroupMember::getGroupCode, groupCode)
               .eq(RecipientGroupMember::getUserId, userId);
        
        RecipientGroupMember member = memberMapper.selectOne(wrapper);
        if (member == null) {
            throw new RuntimeException("组成员不存在: " + userId);
        }

        memberMapper.deleteById(member.getId());

        log.info("组成员删除成功: groupCode={}, userId={}", groupCode, userId);
    }

    /**
     * 转换为成员DTO
     */
    private RecipientGroupMemberDto convertToMemberDto(RecipientGroupMember member) {
        RecipientGroupMemberDto dto = new RecipientGroupMemberDto();
        BeanUtils.copyProperties(member, dto);
        
        // 解析偏好渠道JSON
        if (StringUtils.hasText(member.getPreferredChannels())) {
            try {
                List<String> channels = objectMapper.readValue(member.getPreferredChannels(), 
                        new TypeReference<List<String>>() {});
                dto.setPreferredChannels(channels);
            } catch (JsonProcessingException e) {
                log.warn("偏好渠道JSON反序列化失败", e);
                dto.setPreferredChannels(new ArrayList<>());
            }
        } else {
            dto.setPreferredChannels(new ArrayList<>());
        }
        
        return dto;
    }
}
