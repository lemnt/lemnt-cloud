package com.lemnt.system.adapter.web.auth;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.core.domain.R;
import com.lemnt.common.log.annotation.Log;
import com.lemnt.common.log.enums.BusinessType;
import com.lemnt.common.oss.service.OssFileService;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.common.security.model.SecurityUser;
import com.lemnt.common.security.service.TokenService;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.adapter.web.auth.converter.RoleResConverter;
import com.lemnt.system.adapter.web.auth.converter.UserReqConverter;
import com.lemnt.system.adapter.web.auth.converter.UserResConverter;
import com.lemnt.system.adapter.web.auth.req.*;
import com.lemnt.system.adapter.web.auth.res.*;
import com.lemnt.system.application.auth.converter.UserConverter;
import com.lemnt.system.application.auth.dto.RoleDTO;
import com.lemnt.system.application.auth.dto.UserDTO;
import com.lemnt.system.application.auth.executor.*;
import com.lemnt.system.application.org.executor.DeptTreeQueryExe;
import com.lemnt.system.application.org.qry.DeptListQry;
import com.lemnt.system.domain.auth.model.TreeSelect;
import com.lemnt.system.domain.auth.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 用户信息
 *
 * @author wangxx
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserPageQueryExe userPageQueryExe;
    private final UserDetailQueryExe userDetailQueryExe;
    private final UserAddCmdExe userAddCmdExe;
    private final UserUpdateCmdExe userUpdateCmdExe;
    private final UserDeleteCmdExe userDeleteCmdExe;
    private final UserResetPwdCmdExe userResetPwdCmdExe;
    private final UserUpdateStatusCmdExe userUpdateStatusCmdExe;
    private final UserUpdateProfileCmdExe userUpdateProfileCmdExe;
    private final UserInfoQueryExe userInfoQueryExe;
    private final UserRoleAuthQueryExe userRoleAuthQueryExe;
    private final UserRoleAuthCmdExe userRoleAuthCmdExe;
    private final UserUpdatePwdCmdExe userUpdatePwdCmdExe;
    private final UserUnlockCmdExe userUnlockCmdExe;
    private final UserProfileQueryExe userProfileQueryExe;
    private final UserAvatarUpdateCmdExe userAvatarUpdateCmdExe;
    private final DeptTreeQueryExe deptTreeQueryExe;
    private final OssFileService ossFileService;
    private final TokenService tokenService;

    @Operation(summary = "获取用户列表")
    @RequiresPermissions("system:user:list")
    @GetMapping
    public R<UserRes> list(@Validated UserPageReq req) {
        PageResult<UserDTO> result = userPageQueryExe.execute(UserReqConverter.toQry(req));
        return R.page(UserResConverter.toResList(result.getRecords()), result.getTotal());
    }

    @Operation(summary = "根据用户编号获取详细信息")
    @RequiresPermissions("system:user:query")
    @GetMapping(value = {"/options", "/{userId}"})
    public R<UserDetailRes> getDetail(@PathVariable(value = "userId", required = false) Long userId) {
        return R.ok(UserResConverter.toDetailRes(userDetailQueryExe.execute(userId)));
    }

    @Operation(summary = "获取用户信息（前端路由初始化）")
    @SuppressWarnings("unchecked")
    @GetMapping("/current-info")
    public R<UserInfoRes> getInfo() {
        Map<String, Object> result = userInfoQueryExe.execute();
        UserInfoRes res = new UserInfoRes(
                UserResConverter.toRes(UserConverter.toDTO((User) result.get("user"))),
                (Set<String>) result.get("roles"),
                (Set<String>) result.get("permissions"));
        return R.ok(res);
    }

    @Operation(summary = "获取个人资料")
    @SuppressWarnings("unchecked")
    @GetMapping("/profile")
    public R<UserProfileRes> profile() {
        Map<String, Object> result = userProfileQueryExe.execute();
        UserProfileRes res = new UserProfileRes(
                UserResConverter.toRes(UserConverter.toDTO((User) result.get("user"))),
                (String) result.get("roleGroup"),
                (String) result.get("postGroup")
        );
        return R.ok(res);
    }

    @Operation(summary = "上传用户头像")
    @Log(title = "用户头像", businessType = BusinessType.UPDATE)
    @PostMapping("/profile/avatar")
    public R<UserAvatarRes> avatar(@RequestParam("avatarfile") MultipartFile file) throws IOException {
        String fileUrl = ossFileService.uploadFile(file);
        String oldAvatar = userAvatarUpdateCmdExe.execute(fileUrl);
        if (com.lemnt.common.core.utils.StringUtils.isNotBlank(oldAvatar)) {
            ossFileService.deleteFile(oldAvatar);
        }
        SecurityUser securityUser = tokenService.getSecurityUser();
        if (securityUser != null && securityUser.getDetail() != null) {
            securityUser.getDetail().setAvatar(fileUrl);
            tokenService.setSecurityUser(securityUser);
        }
        return R.ok(new UserAvatarRes(fileUrl));
    }

    @Operation(summary = "获取部门树列表（用于新增用户时选择部门）")
    @RequiresPermissions("system:user:list")
    @GetMapping("/dept-tree")
    public R<List<TreeSelect>> deptTree(DeptListQry qry) {
        return R.ok(deptTreeQueryExe.execute(qry));
    }

    @Operation(summary = "新增用户")
    @RequiresPermissions("system:user:add")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Boolean> add(@Validated @RequestBody UserAddReq req) {
        return R.ok(userAddCmdExe.execute(UserReqConverter.toCmd(req)));
    }

    @Operation(summary = "修改用户")
    @RequiresPermissions("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Boolean> edit(@Validated @RequestBody UserUpdateReq req) {
        return R.ok(userUpdateCmdExe.execute(UserReqConverter.toCmd(req)));
    }

    @Operation(summary = "删除用户")
    @RequiresPermissions("system:user:remove")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public R<Boolean> remove(@PathVariable List<Long> userIds) {
        return R.ok(userDeleteCmdExe.execute(userIds));
    }

    @Operation(summary = "重置密码")
    @RequiresPermissions("system:user:resetPwd")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/reset-password")
    public R<Boolean> resetPwd(@RequestBody UserPwdResetReq req) {
        return R.ok(userResetPwdCmdExe.execute(UserReqConverter.toCmd(req)));
    }

    @Operation(summary = "状态修改")
    @RequiresPermissions("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/change-status")
    public R<Boolean> changeStatus(@Validated @RequestBody UserStatusUpdateReq req) {
        return R.ok(userUpdateStatusCmdExe.execute(UserReqConverter.toCmd(req)));
    }

    @Operation(summary = "根据用户编号获取授权角色")
    @SuppressWarnings("unchecked")
    @RequiresPermissions("system:user:query")
    @GetMapping("/auth-role/{userId}")
    public R<UserAuthRoleRes> authRole(@PathVariable Long userId) {
        Map<String, Object> result = userRoleAuthQueryExe.execute(userId);
        UserAuthRoleRes res = new UserAuthRoleRes(
                UserResConverter.toRes((UserDTO) result.get("user")),
                RoleResConverter.toResList((List<RoleDTO>) result.get("roles")));
        return R.ok(res);
    }

    @Operation(summary = "用户授权角色")
    @RequiresPermissions("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.GRANT)
    @PutMapping("/auth-role")
    public R<Boolean> insertAuthRole(@RequestBody UserRoleAuthReq req) {
        return R.ok(userRoleAuthCmdExe.execute(UserReqConverter.toCmd(req)));
    }

    @Operation(summary = "修改个人信息")
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping("/profile")
    public R<Boolean> updateProfile(@RequestBody UserProfileUpdateReq req) {
        return R.ok(userUpdateProfileCmdExe.execute(UserReqConverter.toCmd(req)));
    }

    @Operation(summary = "修改个人密码")
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping("/profile/update-password")
    public R<Boolean> updatePwd(@RequestBody UserPwdUpdateReq req) {
        return R.ok(userUpdatePwdCmdExe.execute(UserReqConverter.toCmd(req)));
    }

    @Operation(summary = "解锁用户（清除登录失败次数限制）")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("system:user:edit")
    @PutMapping("/unlock/{userName}")
    public R<Boolean> unlock(@PathVariable String userName) {
        return R.ok(userUnlockCmdExe.execute(userName));
    }
}
