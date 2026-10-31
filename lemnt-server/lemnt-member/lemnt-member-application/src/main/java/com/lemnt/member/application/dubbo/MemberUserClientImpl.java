package com.lemnt.member.application.dubbo;

import com.lemnt.member.client.MemberUserClient;
import com.lemnt.member.client.dto.MemberLoginInfoDTO;
import com.lemnt.member.client.dto.MemberUserDTO;
import com.lemnt.member.domain.user.gateway.MemberUserGateway;
import com.lemnt.member.domain.user.model.Member;

import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 会员客户端实现。
 */
@DubboService
@RequiredArgsConstructor
public class MemberUserClientImpl implements MemberUserClient {

    private final MemberUserGateway memberUserGateway;
    private final com.lemnt.member.domain.growth.gateway.MemberLevelGateway memberLevelGateway;

    /**
     * 根据会员编号查询跨服务基础资料。
     */
    @Override
    public MemberUserDTO getByMemberId(Long memberId) {
        Member member = memberUserGateway.findById(memberId);
        if (member == null) {
            return null;
        }
        var level = member.levelId() == null ? null : memberLevelGateway.level(member.levelId());
        int percent = level == null || !Integer.valueOf(1).equals(level.status()) ? 100 : level.discountPercent();
        return new MemberUserDTO(member.id(), member.nickname(), member.status(), percent, member.points(), member.avatar());
    }

    /** 锁定会员行，在同一事务内读取会员资料和密码。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberLoginInfoDTO getByMobile(String mobile) {
        var member = memberUserGateway.findByMobile(mobile);
        if (member == null) {
            return null;
        }
        member = memberUserGateway.lockById(member.id());
        if (member == null || !mobile.equals(member.mobile())) {
            return null;
        }
        String password = memberUserGateway.findPasswordByMemberId(member.id());
        return new MemberLoginInfoDTO(member.id(), member.mobile(), member.status(), password);
    }
    /** 兼容旧私有调用并保留事务，缺少IP时记录未知。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordLogin(Long memberId) { recordLogin(memberId,null); }

    /** 认证中心成功签发会话后记录；状态变化时拒绝并由认证端撤销会话。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordLogin(Long memberId, String loginIp) {
        if (loginIp != null) {
            try {
                if(loginIp.length()>50 || !loginIp.matches("[0-9a-fA-F:.]+") || (!loginIp.contains(":") && !loginIp.matches("[0-9]{1,3}(\\.[0-9]{1,3}){3}"))) throw new IllegalArgumentException();
                java.net.InetAddress.getByName(loginIp);
            } catch(Exception error) {throw new com.lemnt.common.core.exception.ServiceException("登录连接IP不合法",400);}
        }
        var member=memberUserGateway.lockById(memberId);
        if (member == null || !Integer.valueOf(1).equals(member.status())) {
            throw new com.lemnt.common.core.exception.ServiceException("会员不可登录",400);
        }
        memberUserGateway.recordLogin(memberId,memberUserGateway.now(),loginIp);
    }

}
