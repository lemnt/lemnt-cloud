package com.lemnt.member.application.account.executor;

import com.lemnt.member.application.account.qry.*;
import com.lemnt.member.application.account.dto.*;
import com.lemnt.member.application.account.converter.MemberDTOConverter;
import com.lemnt.member.domain.account.gateway.MemberAdminQueryGateway;
import com.lemnt.member.domain.account.model.*;

@org.springframework.stereotype.Component
@lombok.RequiredArgsConstructor
public class MemberAdminQueryExe {
    private final MemberAdminQueryGateway gateway;

    private MemberAdminFilter filter(AdminMemberPageQry q) {
        MemberRules.page(q.pageNum(), q.pageSize());
        if (q.memberId() != null) MemberRules.id(q.memberId());
        if (q.status() != null && q.status() != 0 && q.status() != 1)
            throw new IllegalArgumentException("状态不合法");
        return new MemberAdminFilter(
                q.memberId(),
                MemberRules.optional(q.name(), 100),
                q.status(),
                q.kind(),
                (q.pageNum() - 1) * q.pageSize(),
                q.pageSize());
    }

    public AccountPageDTO<GroupDTO> groups(AdminMemberPageQry q) {
        var f = filter(q);
        return new AccountPageDTO<>(
                gateway.groups(f).stream().map(MemberDTOConverter::group).toList(),
                gateway.groupsCount(f));
    }

    public AccountPageDTO<TagDTO> tags(AdminMemberPageQry q) {
        var f = filter(q);
        return new AccountPageDTO<>(
                gateway.tags(f).stream().map(MemberDTOConverter::tag).toList(),
                gateway.tagsCount(f));
    }

    public AccountPageDTO<AssetRecordDTO> assets(AdminMemberPageQry q) {
        var f = filter(q);
        if (!java.util.Set.of("POINT", "EXPERIENCE").contains(q.kind()))
            throw new IllegalArgumentException("资产类型不合法");
        return new AccountPageDTO<>(
                gateway.assets(f).stream().map(MemberDTOConverter::assetRecord).toList(),
                gateway.assetsCount(f));
    }

    public AssetRecordDTO assetsDetail(AdminMemberRecordDetailQry q) {
        MemberRules.id(q.id());
        var v = gateway.assetsDetail(q.id());
        if (v != null && !java.util.Objects.equals(q.kind(), v.assetType())) return null;
        return MemberDTOConverter.assetRecord(v);
    }

    public AccountPageDTO<LevelRecordDTO> levels(AdminMemberPageQry q) {
        var f = filter(q);
        return new AccountPageDTO<>(
                gateway.levels(f).stream().map(MemberDTOConverter::levelRecord).toList(),
                gateway.levelsCount(f));
    }

    public LevelRecordDTO levelsDetail(AdminMemberRecordDetailQry q) {
        MemberRules.id(q.id());
        var v = gateway.levelsDetail(q.id());
        return MemberDTOConverter.levelRecord(v);
    }

    public AccountPageDTO<SignRecordDTO> signs(AdminMemberPageQry q) {
        var f = filter(q);
        return new AccountPageDTO<>(
                gateway.signs(f).stream().map(MemberDTOConverter::signRecord).toList(),
                gateway.signsCount(f));
    }
}
