package com.lemnt.system.application.auth.executor;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.application.auth.converter.RoleConverter;
import com.lemnt.system.application.auth.dto.RoleDTO;
import com.lemnt.system.application.auth.qry.RolePageQry;
import com.lemnt.system.domain.auth.gateway.RoleGateway;
import com.lemnt.system.domain.auth.model.Role;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 角色分页查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class RolePageQueryExe {
    private final RoleGateway roleGateway;

    /**
     * 执行角色分页查询
     *
     * @param qry 查询条件
     * @return 分页结果
     */
    public PageResult<RoleDTO> execute(RolePageQry qry) {
        Role condition = new Role();
        condition.setRoleName(qry.roleName());
        condition.setRoleKey(qry.roleKey());
        condition.setStatus(qry.status());
        condition.setBeginTime(qry.beginTime());
        condition.setEndTime(qry.endTime());
        PageResult<Role> pageResult = roleGateway.selectPage(condition, new PageQry(qry.pageNum(), qry.pageSize()));
        return PageResult.of(RoleConverter.toDTOList(pageResult.getRecords()), pageResult.getTotal());
    }
}
