package com.lemnt.system.application.auth.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.domain.auth.gateway.UserGateway;
import com.lemnt.system.domain.auth.gateway.UserPostGateway;
import com.lemnt.system.domain.auth.gateway.UserRoleGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 删除用户命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class UserDeleteCmdExe {
    private final UserPostGateway userPostGateway;
    private final UserRoleGateway userRoleGateway;
    private final UserGateway userGateway;

    /**
     * 批量删除用户
     *
     * @param userIds 用户ID数组
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean execute(List<Long> userIds) {
        Long currentUserId = SecurityUtils.getUserId();
        if (CollUtil.isEmpty(userIds)) {
            return Boolean.FALSE;
        }
        for (Long id : userIds) {
            if (id.equals(currentUserId)) {
                throw new ServiceException("当前用户不能删除");
            }
        }
        // 删除关联关系
        userRoleGateway.deleteUserRoleByUserIds(userIds);
        userPostGateway.deleteUserPostByUserIds(userIds);
        return userGateway.deleteUserByIds(userIds);
    }
}
