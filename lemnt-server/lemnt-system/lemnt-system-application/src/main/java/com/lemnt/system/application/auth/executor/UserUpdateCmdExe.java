package com.lemnt.system.application.auth.executor;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.lemnt.system.application.auth.cmd.UserUpdateCmd;
import com.lemnt.system.domain.auth.model.User;
import com.lemnt.system.domain.auth.gateway.UserGateway;
import com.lemnt.system.domain.auth.service.UserDomainService;

/**
 * 修改用户命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class UserUpdateCmdExe {
    private final UserGateway userGateway;

    private final UserDomainService userDomainService;

    /**
     * 执行修改用户
     *
     * @param cmd 修改命令
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean execute(UserUpdateCmd cmd) {
        User entity = new User();
        entity.setUserId(cmd.userId());
        entity.setDeptId(cmd.deptId());
        entity.setNickName(cmd.nickName());
        entity.setEmail(cmd.email());
        entity.setPhonenumber(cmd.phonenumber());
        entity.setSex(cmd.sex());
        entity.setStatus(cmd.status());
        userDomainService.requireById(entity.getUserId());
        userDomainService.checkPhoneUnique(entity.getUserId(), entity.getPhonenumber());
        userDomainService.checkEmailUnique(entity.getUserId(), entity.getEmail());
        // 删除用户原角色和岗位
        userGateway.deleteUserRoleByUserId(entity.getUserId());
        userGateway.deleteUserPostByUserId(entity.getUserId());

        // 重新绑定角色
        if (CollUtil.isNotEmpty(cmd.roleIds())) {
            userGateway.insertUserRoles(entity.getUserId(), cmd.roleIds());
        }
        // 重新绑定岗位
        if (CollUtil.isNotEmpty(cmd.postIds())) {
            userGateway.insertUserPosts(entity.getUserId(), cmd.postIds());
        }
        return userGateway.updateUser(entity);
    }
}
