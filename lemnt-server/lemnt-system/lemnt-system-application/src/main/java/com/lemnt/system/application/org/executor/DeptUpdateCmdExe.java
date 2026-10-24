package com.lemnt.system.application.org.executor;

import java.util.List;
import java.util.Objects;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.application.org.cmd.DeptUpdateCmd;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.lemnt.common.core.constant.UserConstants;
import com.lemnt.common.core.utils.StringUtils;
import com.lemnt.system.domain.org.model.Dept;
import com.lemnt.system.domain.org.gateway.DeptGateway;

/**
 * 修改部门命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DeptUpdateCmdExe {
    private final DeptGateway deptGateway;

    /**
     * 执行修改部门
     *
     * @param cmd 修改命令
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean execute(DeptUpdateCmd cmd) {
        Long deptId = cmd.deptId();
        // 数据权限校验
        Dept condition = new Dept();
        condition.setDeptId(deptId);
        if (!SecurityUtils.isAdmin() && StringUtils.isEmpty(deptGateway.selectDeptList(condition))) {
            throw new ServiceException("没有权限访问部门数据！");
        }
        // 唯一性校验
        Dept unique =
                deptGateway.checkDeptNameUnique(cmd.deptName(), cmd.parentId());
        if (unique != null && !unique.getDeptId().equals(deptId)) {
            throw new ServiceException("修改部门'" + cmd.deptName() + "'失败，部门名称已存在");
        } else if (cmd.parentId().equals(deptId)) {
            throw new ServiceException("修改部门'" + cmd.deptName() + "'失败，上级部门不能是自己");
        } else if (UserConstants.DEPT_DISABLE.equals(cmd.status())
                && deptGateway.selectNormalChildrenDeptById(deptId)) {
            throw new ServiceException("该部门包含未停用的子部门！");
        }
        Dept entity = new Dept();
        entity.setDeptId(cmd.deptId());
        entity.setParentId(cmd.parentId());
        entity.setDeptName(cmd.deptName());
        entity.setOrderNum(cmd.orderNum());
        // 此命令接收完整编辑表单；清空联系字段时显式写空串，排序更新使用独立命令。
        entity.setLeader(cmd.leader() == null ? "" : cmd.leader());
        entity.setPhone(cmd.phone() == null ? "" : cmd.phone());
        entity.setEmail(cmd.email() == null ? "" : cmd.email());
        entity.setStatus(cmd.status());
        entity.setUpdateBy(SecurityUtils.getUsername());

        Dept newParentDept = deptGateway.selectDeptById(cmd.parentId());
        Dept oldDept = deptGateway.selectDeptById(cmd.deptId());
        if (Objects.nonNull(newParentDept) && Objects.nonNull(oldDept)) {
            String newAncestors = newParentDept.getAncestors() + "," + newParentDept.getDeptId();
            String oldAncestors = oldDept.getAncestors();
            entity.setAncestors(newAncestors);
            updateDeptChildren(cmd.deptId(), newAncestors, oldAncestors);
        }

        Boolean result = deptGateway.updateDept(entity);
        if (UserConstants.DEPT_NORMAL.equals(cmd.status()) && StringUtils.isNotEmpty(entity.getAncestors())
                && !StringUtils.equals("0", entity.getAncestors())) {
            // 启用部门时，同步启用所有上级部门
            String ancestors = entity.getAncestors();
            Long[] deptIds = StringUtils.split(ancestors, ",") == null ? new Long[0]
                    : java.util.Arrays.stream(StringUtils.split(ancestors, ","))
                    .map(Long::parseLong).toArray(Long[]::new);
            deptGateway.updateDeptStatusNormal(deptIds);
        }
        return result;
    }

    private void updateDeptChildren(Long deptId, String newAncestors, String oldAncestors) {
        List<Dept> children = deptGateway.selectChildrenDeptById(deptId);
        for (Dept child : children) {
            child.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
        }
        if (!children.isEmpty()) {
            deptGateway.updateDeptChildren(children);
        }
    }
}
