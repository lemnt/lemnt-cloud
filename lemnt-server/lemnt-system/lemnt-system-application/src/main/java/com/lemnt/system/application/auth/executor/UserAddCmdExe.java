package com.lemnt.system.application.auth.executor;

import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.application.auth.cmd.UserAddCmd;
import com.lemnt.system.domain.auth.gateway.UserGateway;
import com.lemnt.system.domain.auth.gateway.UserPostGateway;
import com.lemnt.system.domain.auth.gateway.UserRoleGateway;
import com.lemnt.system.domain.auth.model.User;
import com.lemnt.system.domain.auth.service.UserDomainService;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;

/**
 * 新增用户命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class UserAddCmdExe {
    private final UserPostGateway userPostGateway;
    private final UserRoleGateway userRoleGateway;
    private final UserGateway userGateway;
    private final UserDomainService userDomainService;

    /**
     * 执行新增用户
     *
     * @param cmd 新增命令
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean execute(UserAddCmd cmd) {
        User entity = new User();
        entity.setDeptId(cmd.deptId());
        entity.setUserName(cmd.userName());
        entity.setNickName(cmd.nickName());
        entity.setEmail(cmd.email());
        entity.setPhonenumber(cmd.phonenumber());
        entity.setSex(cmd.sex());
        entity.setPassword(SecurityUtils.encryptPassword(cmd.password()));
        entity.setStatus(cmd.status());
        userDomainService.checkUserNameUnique(entity.getUserName());
        userDomainService.checkPhoneUnique(entity.getPhonenumber());
        userDomainService.checkEmailUnique(entity.getEmail());
        Boolean result = userGateway.insertUser(entity);
        if (result) {
            // 新增用户角色关联
            if (CollUtil.isNotEmpty(cmd.roleIds())) {
                userRoleGateway.insertUserRoles(entity.getUserId(), cmd.roleIds());
            }
            // 新增用户岗位关联
            if (CollUtil.isNotEmpty(cmd.postIds())) {
                userPostGateway.insertUserPosts(entity.getUserId(), cmd.postIds());
            }
        }
        return result;
    }
}
