package com.lemnt.member.infrastructure.user.gateway;

import com.lemnt.auth.api.MemberSessionClient;
import com.lemnt.member.domain.user.gateway.MemberAuthGateway;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/** 仅调用认证中心撤销会话，不在会员服务读取或解析token。 */
@Component
public class MemberAuthGatewayImpl implements MemberAuthGateway {

    @DubboReference
    private MemberSessionClient memberSessionClient;

    /** 同步完成撤销，远程失败由外层会员事务回滚。 */
    @Override
    public void revokeSessions(Long memberId) {
        memberSessionClient.revokeMemberSessions(memberId);
    }
}
