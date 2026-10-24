package com.lemnt.system.application.log.executor;

import com.lemnt.system.application.log.cmd.LogininforAddCmd;
import com.lemnt.system.domain.log.model.Logininfor;
import com.lemnt.system.domain.log.gateway.LogininforGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 新增登录日志命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class LogininforAddCmdExe {

    private final LogininforGateway logininforGateway;

    /**
     * 执行新增登录日志
     *
     * @param cmd 命令对象
     * @return 影响行数
     */
    public Boolean execute(LogininforAddCmd cmd) {
        Logininfor entity = new Logininfor();
        entity.setUserName(cmd.userName());
        entity.setIpaddr(cmd.ipaddr());
        entity.setLoginLocation(cmd.loginLocation());
        entity.setBrowser(cmd.browser());
        entity.setOs(cmd.os());
        entity.setStatus(cmd.status());
        entity.setMsg(cmd.msg());
        return logininforGateway.insert(entity);
    }
}
