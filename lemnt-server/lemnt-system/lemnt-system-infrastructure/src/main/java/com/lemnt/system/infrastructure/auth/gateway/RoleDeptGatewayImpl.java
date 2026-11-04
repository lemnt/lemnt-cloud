package com.lemnt.system.infrastructure.auth.gateway;

import com.lemnt.system.domain.auth.gateway.RoleDeptGateway;
import com.lemnt.system.infrastructure.auth.dataobject.RoleDeptDO;
import com.lemnt.system.infrastructure.auth.mapper.RoleDeptMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.List;

/**
 * RoleDept 持久化实现。
 */
@Component
@RequiredArgsConstructor
public class RoleDeptGatewayImpl implements RoleDeptGateway {
    private final RoleDeptMapper roleDeptMapper;

    @Override
    public Boolean deleteRoleDeptByRoleId(Long roleId) {
        return deleteRoleDeptByRoleIdRow(roleId) > 0;
    }

    @Override
    public Boolean deleteRoleDept(List<Long> ids) {
        return deleteRoleDeptRow(ids) > 0;
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
        return selectCountRoleDeptByDeptIdRow(deptId);
    }

    /**
     * 通过角色ID删除角色部门关联
     *
     * @param roleId 角色ID
     * @return 结果
     */
    private int deleteRoleDeptByRoleIdRow(Long roleId) {
        return roleDeptMapper.delete(
            Wrappers.<RoleDeptDO>lambdaQuery().eq(RoleDeptDO::getRoleId, roleId));
    }

    /**
     * 批量删除角色部门关联
     *
     * @param roleIds 角色ID数组
     * @return 结果
     */
    private int deleteRoleDeptRow(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return 0;
        }
        return roleDeptMapper.delete(
            Wrappers.<RoleDeptDO>lambdaQuery().in(RoleDeptDO::getRoleId, roleIds));
    }

    /**
     * 查询部门被角色使用数量
     *
     * @param deptId 部门ID
     * @return 结果
     */
    private long selectCountRoleDeptByDeptIdRow(Long deptId) {
        return roleDeptMapper.selectCount(
            Wrappers.<RoleDeptDO>lambdaQuery().eq(RoleDeptDO::getDeptId, deptId));
    }
}
