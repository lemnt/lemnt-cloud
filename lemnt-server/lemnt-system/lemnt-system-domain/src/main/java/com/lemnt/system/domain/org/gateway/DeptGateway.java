package com.lemnt.system.domain.org.gateway;

import java.util.List;

import com.lemnt.system.domain.org.model.Dept;

/**
 * 部门管理 Gateway 端口
 *
 * @author wangxx
 */
public interface DeptGateway {

    /**
     * 查询部门列表
     *
     * @param dept 查询条件
     * @return 部门列表
     */
    List<Dept> selectDeptList(Dept dept);

    /**
     * 根据角色ID查询部门树信息
     *
     * @param roleId 角色ID
     * @return 选中部门列表
     */
    List<Long> selectDeptListByRoleId(Long roleId);

    /**
     * 根据部门ID查询信息
     *
     * @param deptId 部门ID
     * @return 部门信息
     */
    Dept selectDeptById(Long deptId);

    /**
     * 根据ID查询所有子部门
     *
     * @param deptId 部门ID
     * @return 子部门列表
     */
    List<Dept> selectChildrenDeptById(Long deptId);

    /**
     * 根据ID查询所有子部门（正常状态）
     *
     * @param deptId 部门ID
     * @return 子部门数
     */
    Boolean selectNormalChildrenDeptById(Long deptId);

    /**
     * 是否存在子节点
     *
     * @param deptId 部门ID
     * @return 结果
     */
    Boolean hasChildByDeptId(Long deptId);

    /**
     * 查询部门是否存在用户
     *
     * @param deptId 部门ID
     * @return 结果
     */
    Boolean checkDeptExistUser(Long deptId);

    /**
     * 校验部门名称是否唯一
     *
     * @param deptName 部门名称
     * @param parentId 父部门ID
     * @return 部门信息
     */
    Dept checkDeptNameUnique(String deptName, Long parentId);

    /**
     * 新增部门
     *
     * @param dept 部门信息
     * @return 是否成功
     */
    Boolean insertDept(Dept dept);

    /**
     * 修改部门
     *
     * @param dept 部门信息
     * @return 是否成功
     */
    Boolean updateDept(Dept dept);

    /**
     * 修改所在部门正常状态
     *
     * @param deptIds 部门ID组
     */
    void updateDeptStatusNormal(Long[] deptIds);

    /**
     * 修改子元素关系
     *
     * @param depts 子元素
     * @return 是否成功
     */
    Boolean updateDeptChildren(List<Dept> depts);

    /**
     * 保存部门排序
     *
     * @param dept 部门信息
     */
    void updateDeptSort(Dept dept);

    /**
     * 删除部门
     *
     * @param deptId 部门ID
     * @return 是否成功
     */
    Boolean deleteDeptById(Long deptId);
}
