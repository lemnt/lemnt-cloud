package com.lemnt.auth.infrastructure.log.gateway;

import com.lemnt.auth.domain.log.enums.LoginMethodEnum;
import com.lemnt.auth.domain.log.gateway.RecordLogGateway;
import com.lemnt.common.core.utils.DateUtils;
import com.lemnt.common.core.utils.ip.IpUtils;
import com.lemnt.system.api.RemoteLogService;
import com.lemnt.system.api.RemoteUserService;
import com.lemnt.system.api.domain.bo.LogininforBo;
import com.lemnt.system.api.domain.bo.UserBo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 记录日志方法
 *
 * @author wangxx
 */
@Service
public class RecordLogGatewayImpl implements RecordLogGateway {

    @DubboReference
    private RemoteLogService remoteLogService;
    @DubboReference
    private RemoteUserService remoteUserService;

    @Override
    public void recordLogininfor(String username, Integer loginType, String status, String message) {
        LogininforBo logininfor = new LogininforBo();
        logininfor.setUserName(username);
        logininfor.setIpaddr(IpUtils.getIpAddr());
        logininfor.setMsg(message);
        // 日志状态
        logininfor.setStatus(status);
        logininfor.setLoginType(loginType);
        logininfor.setLoginMethod(LoginMethodEnum.PASSWORD.getCode());
        logininfor.setAccessTime(LocalDateTime.now());
        remoteLogService.saveLogininfor(logininfor);
    }

    @Override
    public void recordLoginInfo(Long userId) {
        UserBo sysUser = new UserBo();
        sysUser.setUserId(userId);
        // 更新用户登录IP
        sysUser.setLoginIp(IpUtils.getIpAddr());
        // 更新用户登录时间
        sysUser.setLoginDate(DateUtils.getNowDate());
        remoteUserService.recordUserLogin(sysUser);
    }
}
