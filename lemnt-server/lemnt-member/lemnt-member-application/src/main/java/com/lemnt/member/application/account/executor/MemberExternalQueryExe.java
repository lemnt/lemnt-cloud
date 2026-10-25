package com.lemnt.member.application.account.executor;

import com.lemnt.member.application.account.qry.*;
import com.lemnt.member.application.account.dto.*;
import com.lemnt.member.application.account.service.*;

@org.springframework.stereotype.Component
@lombok.RequiredArgsConstructor
public class MemberExternalQueryExe {
    private final MemberIdentityService identities;
    private final MemberSocialService social;

    public java.util.List<String> execute(SocialBindingsQry q) {
        return identities.bindings(q.memberId());
    }

    public java.util.List<SubscribeTemplateDTO> execute(SubscribeTemplatesQry q) {
        return social.templates().stream()
                .map(v -> new SubscribeTemplateDTO(v.id(), v.title(), v.content(), v.type()))
                .toList();
    }
}
