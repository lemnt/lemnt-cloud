package com.lemnt.system.application.org.executor;

import com.lemnt.common.core.constant.UserConstants;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.application.org.cmd.DeptAddCmd;
import com.lemnt.system.domain.org.gateway.DeptGateway;
import com.lemnt.system.domain.org.model.Dept;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * 新增部门命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DeptAddCmdExe {
    private final DeptGateway deptGateway;

    /**
     * 执行新增部门
     *
     * @param cmd 新增命令
     * @return 结果
     */
    public Boolean execute(DeptAddCmd cmd) {
        Dept existing = deptGateway.checkDeptNameUnique(cmd.deptName(), cmd.parentId());
        if (Objects.nonNull(existing)) {
            throw new ServiceException("新增部门'" + cmd.deptName() + "'失败，部门名称已存在");
        }
        Dept entity = new Dept();
        entity.setParentId(cmd.parentId());
        entity.setDeptName(cmd.deptName());
        entity.setOrderNum(cmd.orderNum());
        entity.setLeader(cmd.leader());
        entity.setPhone(cmd.phone());
        entity.setEmail(cmd.email());
        entity.setStatus(cmd.status());
        entity.setCreateBy(SecurityUtils.getUsername());
        Dept parentDept = deptGateway.selectDeptById(cmd.parentId());
        if (parentDept == null || !UserConstants.DEPT_NORMAL.equals(parentDept.getStatus())) {
            throw new ServiceException("部门停用，不允许新增");
        }
        entity.setAncestors(parentDept.getAncestors() + "," + cmd.parentId());
        return deptGateway.insertDept(entity);
    }
}
