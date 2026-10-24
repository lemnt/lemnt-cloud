package com.lemnt.system.application.auth.executor;

import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.domain.auth.gateway.RoleGateway;
import com.lemnt.system.domain.auth.gateway.UserGateway;
import com.lemnt.system.domain.auth.model.Role;
import com.lemnt.system.domain.auth.model.User;
import com.lemnt.system.domain.org.gateway.PostGateway;
import com.lemnt.system.domain.org.model.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 当前登录用户个人资料查询执行器。
 */
@Component
@RequiredArgsConstructor
public class UserProfileQueryExe {

    private final UserGateway userGateway;
    private final RoleGateway roleGateway;
    private final PostGateway postGateway;

    public Map<String, Object> execute() {
        Long userId = SecurityUtils.getUserId();
        User user = userGateway.selectUserById(userId);
        String roleGroup = roleGateway.selectRolesByUserId(userId).stream()
                .map(Role::getRoleName)
                .collect(Collectors.joining(","));
        Set<Long> postIds = Set.copyOf(postGateway.selectPostsByUserId(userId));
        String postGroup = postGateway.selectPostAll().stream()
                .filter(post -> postIds.contains(post.getPostId()))
                .map(Post::getPostName)
                .collect(Collectors.joining(","));

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("roleGroup", roleGroup);
        result.put("postGroup", postGroup);
        return result;
    }
}
