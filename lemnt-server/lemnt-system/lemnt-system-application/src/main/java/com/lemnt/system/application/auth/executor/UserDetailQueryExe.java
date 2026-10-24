package com.lemnt.system.application.auth.executor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.application.auth.converter.RoleConverter;
import com.lemnt.system.application.auth.converter.UserDetailConverter;
import com.lemnt.system.application.auth.dto.UserDetailDTO;
import com.lemnt.system.application.org.converter.PostConverter;
import com.lemnt.system.domain.auth.model.User;
import com.lemnt.system.domain.org.gateway.PostGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.system.domain.auth.model.Role;
import com.lemnt.system.domain.auth.gateway.RoleGateway;
import com.lemnt.system.domain.auth.gateway.UserGateway;

/**
 * 用户详情查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class UserDetailQueryExe {

    private final UserGateway userGateway;
    private final RoleGateway roleGateway;
    private final PostGateway postGateway;

    /**
     * 查询用户详情
     *
     * @param userId 用户ID
     * @return 用户DTO
     */
    public UserDetailDTO execute(Long userId) {
        UserDetailDTO detail = new UserDetailDTO();
        if (Objects.nonNull(userId)) {
            User user = userGateway.selectUserById(userId);
            detail.setDetail(UserDetailConverter.toDTO(user));
            detail.setPostIds(postGateway.selectPostsByUserId(userId));
            detail.setRoleIds(user.getRoles().stream().map(Role::getRoleId).collect(Collectors.toList()));
        }
        List<Role> roleList = roleGateway.selectRoleAll();
        detail.setRoles(SecurityUtils.isAdmin(userId) ? RoleConverter.toDTOList(roleList) : RoleConverter.toDTOList(roleList.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList())));
        detail.setPosts(PostConverter.toDTOList(postGateway.selectPostAll()));
        return detail;
    }
}
