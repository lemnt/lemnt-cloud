package com.lemnt.system.infrastructure.org.gateway;

import com.lemnt.system.domain.org.gateway.DeptGateway;
import com.lemnt.system.domain.org.model.Dept;
import com.lemnt.system.infrastructure.org.converter.DeptConverter;
import com.lemnt.system.infrastructure.org.dataobject.DeptDO;
import com.lemnt.system.infrastructure.org.mapper.DeptMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 部门管理 Gateway 实现
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DeptGatewayImpl implements DeptGateway {
    private final DeptMapper deptMapper;

    @Override
    public List<Dept> selectDeptList(Dept dept) {
        List<DeptDO> doList = deptMapper.selectDeptList(dept);
        return DeptConverter.toEntityList(doList);
    }

    @Override
    public List<Long> selectDeptListByRoleId(Long roleId) {
        return deptMapper.selectDeptListByRoleId(roleId);
    }

    @Override
    public Dept selectDeptById(Long deptId) {
        return DeptConverter.toEntity(deptMapper.selectById(deptId));
    }

    @Override
    public List<Dept> selectChildrenDeptById(Long deptId) {
        return DeptConverter.toEntityList(deptMapper.selectChildrenDeptById(deptId));
    }

    @Override
    public Boolean selectNormalChildrenDeptById(Long deptId) {
        return deptMapper.selectNormalChildrenDeptById(deptId) > 0;
    }

    @Override
    public Boolean hasChildByDeptId(Long deptId) {
        return hasChildByDeptIdRow(deptId) > 0;
    }

    @Override
    public Dept checkDeptNameUnique(String deptName, Long parentId) {
        return DeptConverter.toEntity(checkDeptNameUniqueRow(deptName, parentId));
    }

    @Override
    public Boolean insertDept(Dept dept) {
        return deptMapper.insert(DeptConverter.toDO(dept)) > 0;
    }

    @Override
    public Boolean updateDept(Dept dept) {
        return deptMapper.updateById(DeptConverter.toDO(dept)) > 0;
    }

    @Override
    public void updateDeptStatusNormal(Long[] deptIds) {
        updateDeptStatusNormalRow(deptIds);
    }

    @Override
    public Boolean updateDeptChildren(List<Dept> depts) {
        return updateDeptChildrenRow(DeptConverter.toDOList(depts)) > 0;
    }

    @Override
    public void updateDeptSort(Dept dept) {
        updateDeptSortRow(DeptConverter.toDO(dept));
    }

    @Override
    public Boolean deleteDeptById(Long deptId) {
        return deptMapper.deleteById(deptId) > 0;
    }

    /**
     * 是否存在子节点
     *
     * @param deptId 部门ID
     * @return 结果
     */
    private long hasChildByDeptIdRow(Long deptId) {
        return deptMapper.selectCount(
                Wrappers.<DeptDO>lambdaQuery().eq(DeptDO::getParentId, deptId).last("limit 1"));
    }

    /**
     * 校验部门名称是否唯一
     *
     * @param deptName 部门名称
     * @param parentId 父部门ID
     * @return 结果
     */
    private DeptDO checkDeptNameUniqueRow(String deptName, Long parentId) {
        return deptMapper.selectOne(
                Wrappers.<DeptDO>lambdaQuery()
                        .eq(DeptDO::getDeptName, deptName)
                        .eq(DeptDO::getParentId, parentId)
                        .last("limit 1"));
    }

    /**
     * 修改所在部门正常状态
     *
     * @param deptIds 部门ID组
     */
    private void updateDeptStatusNormalRow(Long[] deptIds) {
        deptMapper.update(
                null,
                Wrappers.<DeptDO>lambdaUpdate()
                        .in(DeptDO::getDeptId, List.of(deptIds))
                        .set(DeptDO::getStatus, "1"));
    }

    /**
     * 修改子元素关系
     *
     * @param depts 子元素
     * @return 结果
     */
    private int updateDeptChildrenRow(List<DeptDO> depts) {
        return deptMapper.updateById(depts).size();
    }

    /**
     * 保存部门排序
     *
     * @param dept 部门信息
     */
    private void updateDeptSortRow(DeptDO dept) {
        deptMapper.update(
                null,
                Wrappers.<DeptDO>lambdaUpdate()
                        .in(DeptDO::getDeptId, dept.getDeptId())
                        .set(DeptDO::getOrderNum, dept.getOrderNum()));
    }
}
