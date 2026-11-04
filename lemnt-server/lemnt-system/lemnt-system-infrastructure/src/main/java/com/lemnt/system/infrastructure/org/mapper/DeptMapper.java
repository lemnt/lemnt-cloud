package com.lemnt.system.infrastructure.org.mapper;

import com.lemnt.common.datascope.annotation.DataScope;
import com.lemnt.system.domain.org.model.Dept;
import com.lemnt.system.infrastructure.org.dataobject.DeptDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Dept 数据层
 *
 * @author wangxx
 */
@Mapper
public interface DeptMapper extends BaseMapper<DeptDO> {
    /**
     * 查询部门管理数据
     *
     * @param dept 部门信息
     * @return 部门信息集合
     */
    @DataScope(deptAlias = "d")
    List<DeptDO> selectDeptList(Dept dept);

    /**
     * 根据角色ID查询部门树信息
     *
     * @param roleId 角色ID
     * @return 选中部门列表
     */
    List<Long> selectDeptListByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据ID查询所有子部门
     *
     * @param deptId 部门ID
     * @return 部门列表
     */
    List<DeptDO> selectChildrenDeptById(Long deptId);

    /**
     * 根据ID查询所有子部门（正常状态）
     *
     * @param deptId 部门ID
     * @return 子部门数
     */
    int selectNormalChildrenDeptById(Long deptId);
}
