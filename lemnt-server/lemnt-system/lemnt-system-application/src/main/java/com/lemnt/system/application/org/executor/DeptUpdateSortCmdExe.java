package com.lemnt.system.application.org.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.system.domain.org.gateway.DeptGateway;
import com.lemnt.system.domain.org.model.Dept;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 保存部门排序命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DeptUpdateSortCmdExe {
    private final DeptGateway deptGateway;

    /**
     * 执行保存部门排序
     *
     * @param deptIds   部门ID数组
     * @param orderNums 排序数组
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean execute(List<Long> deptIds, List<Integer> orderNums) {
        try {
            for (int i = 0; i < deptIds.size(); i++) {
                Dept dept = new Dept();
                dept.setDeptId(deptIds.get(i));
                dept.setOrderNum(orderNums.get(i));
                deptGateway.updateDeptSort(dept);
            }
        } catch (Exception e) {
            throw new ServiceException("保存排序异常，请联系管理员");
        }
        return true;
    }
}
