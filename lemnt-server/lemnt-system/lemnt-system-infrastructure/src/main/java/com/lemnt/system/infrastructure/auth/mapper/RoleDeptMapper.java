package com.lemnt.system.infrastructure.auth.mapper;

import cn.hutool.core.collection.CollUtil;
import com.lemnt.system.infrastructure.auth.dataobject.RoleDeptDO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * RoleRelation 数据层
 *
 * @author wangxx
 */
@Mapper
public interface RoleDeptMapper extends BaseMapper<RoleDeptDO> {

    /**
     * 通过角色ID删除角色部门关联
     *
     * @param roleId 角色ID
     * @return 结果
     */
    default int deleteRoleDeptByRoleId(Long roleId) {
        return this.delete(Wrappers.<RoleDeptDO>lambdaQuery()
                .eq(RoleDeptDO::getRoleId, roleId));
    }

    /**
     * 批量删除角色部门关联
     *
     * @param roleIds 角色ID数组
     * @return 结果
     */
    default int deleteRoleDept(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return 0;
        }
        return this.delete(Wrappers.<RoleDeptDO>lambdaQuery()
                .in(RoleDeptDO::getRoleId, roleIds));
    }

    /**
     * 查询部门被角色使用数量
     *
     * @param deptId 部门ID
     * @return 结果
     */
    default long selectCountRoleDeptByDeptId(Long deptId) {
        return this.selectCount(Wrappers.<RoleDeptDO>lambdaQuery()
                .eq(RoleDeptDO::getDeptId, deptId));
    }
}
