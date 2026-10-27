package com.lemnt.member.application.user.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.application.user.cmd.MemberRegisterCmd;
import com.lemnt.member.application.user.converter.MemberUserDTOConverter;
import com.lemnt.member.application.user.dto.MemberDTO;
import com.lemnt.member.domain.shared.model.MemberRules;
import com.lemnt.member.domain.user.gateway.MemberUserGateway;
import com.lemnt.member.domain.user.model.Member;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * 会员注册命令执行器。
 */
@Component
@RequiredArgsConstructor
public class MemberRegisterCmdExe {

    private final MemberUserGateway memberUserGateway;

    /**
     * 执行会员注册命令。
     */
    @Transactional(rollbackFor = Exception.class)
    public MemberDTO execute(MemberRegisterCmd cmd) {
        if (cmd == null) {
            throw new ServiceException("注册参数不能为空", 400);
        }
        if (cmd.mobile() != null
                && !cmd.mobile().isBlank()
                && memberUserGateway.findByMobile(cmd.mobile().trim()) != null) {
            throw new ServiceException("手机号已注册", 400);
        }
        if (cmd.email() != null
                && !cmd.email().isBlank()
                && memberUserGateway.findByEmail(cmd.email().trim()) != null) {
            throw new ServiceException("邮箱已注册", 400);
        }
        String no =
                "M" + UUID.randomUUID().toString().replace("-", "").substring(0, 24).toUpperCase();
        if (cmd.mobile() != null && !cmd.mobile().isBlank()) {
            MemberRules.mobile(cmd.mobile());
        }
        var member =
                memberUserGateway.save(
                        Member.register(
                                no, cmd.nickname(), cmd.mobile(), cmd.email(), cmd.avatar()));
        return MemberUserDTOConverter.toMemberDTO(
                memberUserGateway.findById(member.id()));
    }
}
