package com.lemnt.system.application.log.executor;

import com.lemnt.system.domain.log.gateway.OperLogGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 批量删除操作日志命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class OperLogDeleteCmdExe {

    private final OperLogGateway operLogGateway;

    /**
     * 执行批量删除操作日志
     *
     * @param operIds 操作日志ID数组
     * @return 影响行数
     */
    public Boolean execute(List<Long> operIds) {
        return operLogGateway.deleteByIds(operIds);
    }
}
