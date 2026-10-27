package com.lemnt.system.adapter.web.auth;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.log.annotation.Log;
import com.lemnt.common.log.enums.BusinessType;
import com.lemnt.common.oss.service.OssFileService;
import com.lemnt.common.security.model.SecurityUser;
import com.lemnt.common.security.service.TokenService;
import com.lemnt.system.adapter.web.auth.converter.UserReqConverter;
import com.lemnt.system.adapter.web.auth.converter.UserResConverter;
import com.lemnt.system.adapter.web.auth.req.*;
import com.lemnt.system.adapter.web.auth.res.*;
import com.lemnt.system.application.auth.converter.UserConverter;
import com.lemnt.system.application.auth.executor.*;
import com.lemnt.system.domain.auth.model.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Map;

/** 个人资料接口。 */
@Tag(name = "个人资料")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserUpdateProfileCmdExe userUpdateProfileCmdExe;
    private final UserUpdatePwdCmdExe userUpdatePwdCmdExe;
    private final UserProfileQueryExe userProfileQueryExe;
    private final UserAvatarUpdateCmdExe userAvatarUpdateCmdExe;
    private final OssFileService ossFileService;
    private final TokenService tokenService;

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
}
