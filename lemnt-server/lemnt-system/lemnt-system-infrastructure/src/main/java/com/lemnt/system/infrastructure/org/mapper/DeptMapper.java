package com.lemnt.system.infrastructure.org.mapper;

import com.lemnt.system.domain.org.model.Dept;
import com.lemnt.system.infrastructure.org.dataobject.DeptDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

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
    List<DeptDO> selectDeptList(Dept dept);

    /**
     * 根据角色ID查询部门树信息
     *
     * @param roleId            角色ID
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

    /**
     * 是否存在子节点
     *
     * @param deptId 部门ID
     * @return 结果
     */
    default long hasChildByDeptId(Long deptId) {
        return selectCount(Wrappers.<DeptDO>lambdaQuery().eq(DeptDO::getParentId, deptId)
                .last("limit 1"));
    }

    /**
     * 校验部门名称是否唯一
     *
     * @param deptName 部门名称
     * @param parentId 父部门ID
     * @return 结果
     */
    default DeptDO checkDeptNameUnique(String deptName, Long parentId) {
        return selectOne(Wrappers.<DeptDO>lambdaQuery()
                .eq(DeptDO::getDeptName, deptName)
                .eq(DeptDO::getParentId, parentId)
                .last("limit 1"));
    }

    /**
     * 修改所在部门正常状态
     *
     * @param deptIds 部门ID组
     */
    default void updateDeptStatusNormal(Long[] deptIds) {
        update(null, Wrappers.<DeptDO>lambdaUpdate()
                .in(DeptDO::getDeptId, List.of(deptIds))
                .set(DeptDO::getStatus, "1"));
    }

    /**
     * 修改子元素关系
     *
     * @param depts 子元素
     * @return 结果
     */
    default int updateDeptChildren(List<DeptDO> depts) {
        return this.updateById(depts).size();
    }

    /**
     * 保存部门排序
     *
     * @param dept 部门信息
     */
    default void updateDeptSort(DeptDO dept) {
        update(null, Wrappers.<DeptDO>lambdaUpdate()
                .in(DeptDO::getDeptId, dept.getDeptId())
                .set(DeptDO::getOrderNum, dept.getOrderNum()));
    }
}
