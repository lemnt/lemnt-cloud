package com.lemnt.system.application.auth.executor;

import com.lemnt.system.application.org.service.TreeDeptService;
import com.lemnt.system.domain.org.gateway.DeptGateway;
import com.lemnt.system.domain.org.model.Dept;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色部门树查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class RoleDeptTreeQueryExe {
    private final DeptGateway deptGateway;
    private final TreeDeptService treeDeptService;

    /**
     * 查询角色部门树
     *
     * @param roleId 角色ID
     * @return 已选中部门ID和完整部门树
     */
    public Map<String, Object> execute(Long roleId) {
        List<Long> checkedKeys = deptGateway.selectDeptListByRoleId(roleId);
        List<Dept> deptList = deptGateway.selectDeptList(new Dept());
        Map<String, Object> result = new HashMap<>();
        result.put("checkedKeys", checkedKeys);
        result.put("depts", treeDeptService.buildTreeSelect(deptList));
        return result;
    }
}
