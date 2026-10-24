package com.lemnt.system.infrastructure.org.gateway;

import java.util.List;

import com.lemnt.system.infrastructure.auth.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.system.domain.org.model.Dept;
import com.lemnt.system.domain.org.gateway.DeptGateway;
import com.lemnt.system.infrastructure.org.converter.DeptConverter;
import com.lemnt.system.infrastructure.org.dataobject.DeptDO;
import com.lemnt.system.infrastructure.org.mapper.DeptMapper;

/**
 * 部门管理 Gateway 实现
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DeptGatewayImpl implements DeptGateway {

    private final DeptMapper deptMapper;
    private final UserMapper userMapper;

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
        return deptMapper.hasChildByDeptId(deptId) > 0;
    }

    @Override
    public Boolean checkDeptExistUser(Long deptId) {
        return userMapper.checkDeptExistUser(deptId) > 0;
    }

    @Override
    public Dept checkDeptNameUnique(String deptName, Long parentId) {
        return DeptConverter.toEntity(deptMapper.checkDeptNameUnique(deptName, parentId));
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
        deptMapper.updateDeptStatusNormal(deptIds);
    }

    @Override
    public Boolean updateDeptChildren(List<Dept> depts) {
        return deptMapper.updateDeptChildren(DeptConverter.toDOList(depts)) > 0;
    }

    @Override
    public void updateDeptSort(Dept dept) {
        deptMapper.updateDeptSort(DeptConverter.toDO(dept));
    }

    @Override
    public Boolean deleteDeptById(Long deptId) {
        return deptMapper.deleteById(deptId) > 0;
    }
}
