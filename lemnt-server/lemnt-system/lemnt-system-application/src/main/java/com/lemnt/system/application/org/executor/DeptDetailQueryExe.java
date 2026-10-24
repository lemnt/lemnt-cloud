package com.lemnt.system.application.org.executor;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.core.utils.StringUtils;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.domain.org.model.Dept;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.system.application.org.converter.DeptConverter;
import com.lemnt.system.application.org.dto.DeptDTO;
import com.lemnt.system.domain.org.gateway.DeptGateway;

/**
 * 部门详情查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DeptDetailQueryExe {
    private final DeptGateway deptGateway;

    /**
     * 查询部门详情
     *
     * @param deptId 部门ID
     * @return 部门DTO
     */
    public DeptDTO execute(Long deptId) {
        Dept condition = new Dept();
        condition.setDeptId(deptId);
        if (!SecurityUtils.isAdmin() && StringUtils.isEmpty(deptGateway.selectDeptList(condition))) {
            throw new ServiceException("没有权限访问部门数据！");
        }
        return DeptConverter.toDTO(deptGateway.selectDeptById(deptId));
    }
}
