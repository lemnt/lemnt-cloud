package com.lemnt.system.application.org.executor;

import com.lemnt.system.application.org.qry.DeptListQry;
import com.lemnt.system.application.org.service.TreeDeptService;
import com.lemnt.system.domain.auth.model.TreeSelect;
import com.lemnt.system.domain.org.gateway.DeptGateway;
import com.lemnt.system.domain.org.model.Dept;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 部门树查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DeptTreeQueryExe {
    private final DeptGateway deptGateway;
    private final TreeDeptService treeDeptService;

    /**
     * 查询部门树列表
     *
     * @param qry 查询条件
     * @return 部门列表
     */
    public List<TreeSelect> execute(DeptListQry qry) {
        Dept dept = new Dept();
        dept.setDeptName(qry.deptName());
        dept.setStatus(qry.status());
        List<Dept> deptList = deptGateway.selectDeptList(dept);
        return treeDeptService.buildTreeSelect(deptList);
    }
}
