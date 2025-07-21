package com.enterprise.notification.admin.controller;

import com.enterprise.notification.admin.dto.common.PageResult;
import com.enterprise.notification.admin.dto.group.*;
import com.enterprise.notification.admin.service.RecipientGroupAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 收件人组管理控制器
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/recipient-groups")
@Validated
@Tag(name = "收件人组管理", description = "收件人组和组成员的增删改查功能")
public class RecipientGroupAdminController {

    @Autowired
    private RecipientGroupAdminService recipientGroupAdminService;

    /**
     * 分页查询收件人组
     */
    @Operation(summary = "分页查询收件人组", description = "支持按组代码、名称等条件分页查询收件人组")
    @PostMapping("/query")
    public ResponseEntity<PageResult<RecipientGroupDto>> getRecipientGroups(
            @Parameter(description = "查询条件") @Valid @RequestBody RecipientGroupQueryRequest request) {

        log.info("查询收件人组列表: current={}, size={}, groupCode={}, groupName={}, isEnabled={}",
                request.getCurrent(), request.getSize(), request.getGroupCode(),
                request.getGroupName(), request.getIsEnabled());

        PageResult<RecipientGroupDto> result = recipientGroupAdminService.getRecipientGroups(
                request.getCurrent(), request.getSize(), request.getGroupCode(),
                request.getGroupName(), request.getIsEnabled());

        return ResponseEntity.ok(result);
    }

    /**
     * 获取收件人组详情
     */
    @GetMapping("/{groupCode}")
    public ResponseEntity<RecipientGroupDto> getRecipientGroup(@PathVariable String groupCode) {
        log.info("获取收件人组详情: groupCode={}", groupCode);

        RecipientGroupDto group = recipientGroupAdminService.getRecipientGroup(groupCode);
        return ResponseEntity.ok(group);
    }

    /**
     * 创建收件人组
     */
    @PostMapping
    public ResponseEntity<RecipientGroupDto> createRecipientGroup(@Valid @RequestBody RecipientGroupCreateRequest request) {
        log.info("创建收件人组: groupCode={}, groupName={}", request.getGroupCode(), request.getGroupName());

        RecipientGroupDto group = recipientGroupAdminService.createRecipientGroup(request);
        return ResponseEntity.ok(group);
    }

    /**
     * 更新收件人组
     */
    @PutMapping("/{groupCode}")
    public ResponseEntity<RecipientGroupDto> updateRecipientGroup(@PathVariable String groupCode,
                                                                 @Valid @RequestBody RecipientGroupUpdateRequest request) {
        log.info("更新收件人组: groupCode={}, groupName={}", groupCode, request.getGroupName());

        RecipientGroupDto group = recipientGroupAdminService.updateRecipientGroup(groupCode, request);
        return ResponseEntity.ok(group);
    }

    /**
     * 删除收件人组
     */
    @DeleteMapping("/{groupCode}")
    public ResponseEntity<Void> deleteRecipientGroup(@PathVariable String groupCode) {
        log.info("删除收件人组: groupCode={}", groupCode);

        recipientGroupAdminService.deleteRecipientGroup(groupCode);
        return ResponseEntity.ok().build();
    }

    /**
     * 查询组成员
     */
    @PostMapping("/members/query")
    public ResponseEntity<PageResult<RecipientGroupMemberDto>> getGroupMembers(@Valid @RequestBody GroupMemberQueryRequest request) {

        log.info("查询组成员: groupCode={}, current={}, size={}",
                request.getGroupCode(), request.getCurrent(), request.getSize());

        PageResult<RecipientGroupMemberDto> result = recipientGroupAdminService.getGroupMembers(
                request.getGroupCode(), request.getCurrent(), request.getSize());

        return ResponseEntity.ok(result);
    }

    /**
     * 添加组成员
     */
    @PostMapping("/{groupCode}/members")
    public ResponseEntity<RecipientGroupMemberDto> addGroupMember(@PathVariable String groupCode,
                                                                 @Valid @RequestBody RecipientGroupMemberCreateRequest request) {
        log.info("添加组成员: groupCode={}, userId={}", groupCode, request.getUserId());

        RecipientGroupMemberDto member = recipientGroupAdminService.addGroupMember(groupCode, request);
        return ResponseEntity.ok(member);
    }

    /**
     * 更新组成员
     */
    @PutMapping("/{groupCode}/members/{userId}")
    public ResponseEntity<RecipientGroupMemberDto> updateGroupMember(@PathVariable String groupCode,
                                                                    @PathVariable String userId,
                                                                    @Valid @RequestBody RecipientGroupMemberUpdateRequest request) {
        log.info("更新组成员: groupCode={}, userId={}", groupCode, userId);

        RecipientGroupMemberDto member = recipientGroupAdminService.updateGroupMember(groupCode, userId, request);
        return ResponseEntity.ok(member);
    }

    /**
     * 删除组成员
     */
    @DeleteMapping("/{groupCode}/members/{userId}")
    public ResponseEntity<Void> deleteGroupMember(@PathVariable String groupCode,
                                                 @PathVariable String userId) {
        log.info("删除组成员: groupCode={}, userId={}", groupCode, userId);

        recipientGroupAdminService.deleteGroupMember(groupCode, userId);
        return ResponseEntity.ok().build();
    }
}
