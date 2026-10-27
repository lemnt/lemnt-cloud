package com.lemnt.system.infrastructure.auth.gateway;

import com.lemnt.system.domain.auth.gateway.RoleDeptGateway;
import com.lemnt.system.infrastructure.auth.dataobject.RoleDeptDO;
import com.lemnt.system.infrastructure.auth.mapper.RoleDeptMapper;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;

import java.util.*;

/** RoleDept 持久化实现。 */
@Component
@RequiredArgsConstructor
public class RoleDeptGatewayImpl implements RoleDeptGateway {
    private final RoleDeptMapper roleDeptMapper;

    @Override
    public Boolean deleteRoleDeptByRoleId(Long roleId) {
        return roleDeptMapper.deleteRoleDeptByRoleId(roleId) > 0;
    }

    @Override
    public Boolean deleteRoleDept(List<Long> ids) {
        return roleDeptMapper.deleteRoleDept(ids) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertRoleDepts(Long roleId, List<Long> deptIds) {
        if (CollUtil.isEmpty(deptIds)) {
            return true;
        }
        List<RoleDeptDO> list = new ArrayList<>();
        for (Long deptId : deptIds) {
            RoleDeptDO rd = new RoleDeptDO();
            rd.setRoleId(roleId);
            rd.setDeptId(deptId);
            list.add(rd);
        }
        roleDeptMapper.insert(list);
        return true;
    }
    @Override
    public Long selectCountRoleDeptByDeptId(Long deptId) {
        return roleDeptMapper.selectCountRoleDeptByDeptId(deptId);
    }
}
