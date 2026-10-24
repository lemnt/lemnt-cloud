package com.lemnt.system.application.log.executor;

import com.lemnt.system.application.log.converter.OperLogConverter;
import com.lemnt.system.application.log.dto.OperLogDTO;
import com.lemnt.system.domain.log.gateway.OperLogGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 操作日志详情查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class OperLogDetailQueryExe {

    private final OperLogGateway operLogGateway;

    /**
     * 执行操作日志详情查询
     *
     * @param operId 操作日志ID
     * @return 操作日志详情
     */
    public OperLogDTO execute(Long operId) {
        return OperLogConverter.toDTO(operLogGateway.selectById(operId));
    }
}
