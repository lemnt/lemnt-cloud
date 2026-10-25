package com.lemnt.member.application.account.executor;

import com.lemnt.member.application.account.cmd.*;
import com.lemnt.member.application.account.dto.*;
import com.lemnt.member.application.account.converter.MemberDTOConverter;
import com.lemnt.member.application.account.service.*;
import com.lemnt.member.domain.account.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberAuthCmdExe {
    private final MemberAuthService service;

    public TokenPairDTO execute(AuthLoginCmd c) {
        return MemberDTOConverter.tokenPair(service.login(c.mobile(), c.password()));
    }

    public TokenPairDTO execute(AuthRefreshCmd c) {
        return MemberDTOConverter.tokenPair(service.refresh(c.refreshToken()));
    }

    public Boolean execute(AuthLogoutCmd c) {
        return service.logout(c.authorization());
    }

    public Boolean execute(PasswordResetCmd c) {
        return service.resetPassword(c.memberId(), c.password());
    }

    public Boolean execute(PasswordChangeCmd c) {
        return service.changePassword(c.memberId(), c.oldPassword(), c.newPassword());
    }
}
