package com.lemnt.system.application.org.executor;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.system.application.org.converter.DeptConverter;
import com.lemnt.system.application.org.dto.DeptDTO;
import com.lemnt.system.application.org.qry.DeptListQry;
import com.lemnt.system.domain.org.model.Dept;
import com.lemnt.system.domain.org.gateway.DeptGateway;

/**
 * 部门列表查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DeptListQueryExe {
    private final DeptGateway deptGateway;

    /**
     * 查询部门列表（平铺）
     *
     * @param qry 查询条件
     * @return 部门列表
     */
    public List<DeptDTO> execute(DeptListQry qry) {
        Dept condition = new Dept();
        condition.setDeptName(qry.deptName());
        condition.setStatus(qry.status());
        List<Dept> depts = deptGateway.selectDeptList(condition);
        return DeptConverter.toDTOList(depts);
    }

    /**
     * 查询部门树（树形结构）
     *
     * @param qry 查询条件
     * @return 树形部门列表
     */
    public List<DeptDTO> executeTree(DeptListQry qry) {
        Dept condition = new Dept();
        condition.setDeptName(qry.deptName());
        condition.setStatus(qry.status());
        List<Dept> depts = deptGateway.selectDeptList(condition);
        List<Dept> tree = Dept.buildDeptTree(depts);
        return DeptConverter.toDTOList(tree);
    }
}
