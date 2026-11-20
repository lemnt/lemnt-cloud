package com.lemnt.system.application.log.executor;

import com.lemnt.system.application.log.cmd.OperLogAddCmd;
import com.lemnt.system.domain.log.gateway.OperLogGateway;
import com.lemnt.system.domain.log.model.OperLog;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 新增操作日志命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class OperLogAddCmdExe {
    private final OperLogGateway operLogGateway;

    /**
     * 执行新增操作日志
     *
     * @param cmd 命令对象
     * @return 影响行数
     */
    public Boolean execute(OperLogAddCmd cmd) {
        OperLog entity = new OperLog();
        entity.setTitle(cmd.title());
        entity.setBusinessType(cmd.businessType());
        entity.setMethod(cmd.method());
        entity.setRequestMethod(cmd.requestMethod());
        entity.setOperatorType(cmd.operatorType());
        entity.setOperName(cmd.operName());
        entity.setDeptName(cmd.deptName());
        entity.setOperUrl(cmd.operUrl());
        entity.setOperIp(cmd.operIp());
        entity.setOperLocation(cmd.operLocation());
        entity.setOperParam(cmd.operParam());
        entity.setJsonResult(cmd.jsonResult());
        entity.setStatus(cmd.status());
        entity.setErrorMsg(cmd.errorMsg());
        entity.setOperTime(cmd.operTime());
        entity.setCostTime(cmd.costTime());
        return operLogGateway.insert(entity);
    }
}
