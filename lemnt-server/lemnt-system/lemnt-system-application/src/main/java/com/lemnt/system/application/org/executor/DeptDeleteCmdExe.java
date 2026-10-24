package com.lemnt.system.application.org.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.core.utils.StringUtils;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.domain.org.model.Dept;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.system.domain.org.gateway.DeptGateway;

/**
 * 删除部门命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DeptDeleteCmdExe {
    private final DeptGateway deptGateway;

    /**
     * 执行删除部门
     *
     * @param deptId 部门ID
     * @return 结果
     */
    public Boolean execute(Long deptId) {
        if (deptGateway.hasChildByDeptId(deptId)) {
            throw new ServiceException("存在下级部门,不允许删除");
        }
        if (deptGateway.checkDeptExistUser(deptId)) {
            throw new ServiceException("部门存在用户,不允许删除");
        }
        // 数据权限校验
        Dept condition = new Dept();
        condition.setDeptId(deptId);
        if (!SecurityUtils.isAdmin() && StringUtils.isEmpty(deptGateway.selectDeptList(condition))) {
            throw new ServiceException("没有权限访问部门数据！");
        }
        return deptGateway.deleteDeptById(deptId);
    }
}
