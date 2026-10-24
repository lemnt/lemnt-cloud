package com.lemnt.system.application.auth.executor;

import java.util.List;

import com.lemnt.system.application.auth.converter.RoleConverter;
import com.lemnt.system.application.auth.dto.RoleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.system.domain.auth.gateway.RoleGateway;

/**
 * 角色选项列表查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class RoleOptionselectQueryExe {

    private final RoleGateway roleGateway;

    /**
     * 查询角色选项列表
     *
     * @return 角色列表
     */
    public List<RoleDTO> execute() {
        return RoleConverter.toDTOList(roleGateway.selectRoleAll());
    }
}
