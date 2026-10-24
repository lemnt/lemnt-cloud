package com.lemnt.system.application.auth.executor;

import com.lemnt.system.domain.auth.service.RoleDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.system.application.auth.converter.RoleConverter;
import com.lemnt.system.application.auth.dto.RoleDTO;
import com.lemnt.system.domain.auth.model.Role;
import com.lemnt.system.domain.auth.gateway.RoleGateway;

/**
 * 角色详情查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class RoleDetailQueryExe {
    private final RoleGateway roleGateway;
    private final RoleDomainService roleDomainService;

    public RoleDTO execute(Long roleId) {
        roleDomainService.checkRoleDataScope(roleId);
        Role entity = roleGateway.selectRoleById(roleId);
        return RoleConverter.toDTO(entity);
    }
}
