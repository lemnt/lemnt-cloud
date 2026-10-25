package com.lemnt.member.infrastructure.account.gateway;

import com.lemnt.member.domain.account.gateway.MemberAdminQueryGateway;
import com.lemnt.member.domain.account.model.*;
import com.lemnt.member.infrastructure.account.mapper.MemberAdminQueryMapper;
import com.lemnt.member.infrastructure.account.converter.*;
import java.util.List;

@org.springframework.stereotype.Component
@lombok.RequiredArgsConstructor
public class MemberAdminQueryGatewayImpl implements MemberAdminQueryGateway {
    private final MemberAdminQueryMapper mapper;

    public List<Group> groups(MemberAdminFilter f) {
        return mapper.groups(f).stream().map(GroupConverter::domain).toList();
    }

    public long groupsCount(MemberAdminFilter f) {
        return mapper.groupsCount(f);
    }

    public List<Tag> tags(MemberAdminFilter f) {
        return mapper.tags(f).stream().map(TagConverter::domain).toList();
    }

    public long tagsCount(MemberAdminFilter f) {
        return mapper.tagsCount(f);
    }

    public List<AssetRecord> assets(MemberAdminFilter f) {
        return mapper.assets(f).stream().map(AssetRecordConverter::domain).toList();
    }

    public long assetsCount(MemberAdminFilter f) {
        return mapper.assetsCount(f);
    }

    public AssetRecord assetsDetail(Long id) {
        return AssetRecordConverter.domain(mapper.assetsDetail(id));
    }

    public List<LevelRecord> levels(MemberAdminFilter f) {
        return mapper.levels(f).stream().map(LevelRecordConverter::domain).toList();
    }

    public long levelsCount(MemberAdminFilter f) {
        return mapper.levelsCount(f);
    }

    public LevelRecord levelsDetail(Long id) {
        return LevelRecordConverter.domain(mapper.levelsDetail(id));
    }

    public List<SignRecord> signs(MemberAdminFilter f) {
        return mapper.signs(f).stream().map(SignRecordConverter::domain).toList();
    }

    public long signsCount(MemberAdminFilter f) {
        return mapper.signsCount(f);
    }
}
