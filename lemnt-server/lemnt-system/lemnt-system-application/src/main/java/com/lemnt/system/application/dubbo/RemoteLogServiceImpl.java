package com.lemnt.system.application.dubbo;

import com.lemnt.system.api.RemoteLogService;
import com.lemnt.system.api.domain.bo.LogininforBo;
import com.lemnt.system.api.domain.bo.OperLogBo;
import com.lemnt.system.domain.log.gateway.LogininforGateway;
import com.lemnt.system.domain.log.gateway.OperLogGateway;
import com.lemnt.system.domain.log.model.Logininfor;
import com.lemnt.system.domain.log.model.OperLog;

import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 日志远程服务实现
 *
 * @author wangxx
 */
@Slf4j
@Component
@DubboService
@RequiredArgsConstructor
public class RemoteLogServiceImpl implements RemoteLogService {
    private final OperLogGateway operLogGateway;
    private final LogininforGateway logininforGateway;

    @Override
    public Boolean saveLog(OperLogBo bo) {
        OperLog entity = new OperLog();
        entity.setTitle(bo.getTitle());
        entity.setBusinessType(bo.getBusinessType());
        entity.setMethod(bo.getMethod());
        entity.setRequestMethod(bo.getRequestMethod());
        entity.setOperatorType(bo.getOperatorType());
        entity.setOperName(bo.getOperName());
        entity.setDeptName(bo.getDeptName());
        entity.setOperUrl(bo.getOperUrl());
        entity.setOperIp(bo.getOperIp());
        entity.setOperParam(bo.getOperParam());
        entity.setJsonResult(bo.getJsonResult());
        entity.setStatus(bo.getStatus());
        entity.setErrorMsg(bo.getErrorMsg());
        return operLogGateway.insert(entity);
    }

    /**
     * 保存登录日志，沿用调用方记录的实际访问时间。
     */
    @Override
    public Boolean saveLogininfor(LogininforBo bo) {
        Logininfor entity = new Logininfor();
        entity.setUserName(bo.getUserName());
        entity.setIpaddr(bo.getIpaddr());
        entity.setStatus(bo.getStatus());
        entity.setMsg(bo.getMsg());
        entity.setAccessTime(bo.getAccessTime());
        return logininforGateway.insert(entity);
    }
}
