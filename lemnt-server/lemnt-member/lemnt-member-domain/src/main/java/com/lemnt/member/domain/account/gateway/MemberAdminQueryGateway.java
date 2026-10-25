package com.lemnt.member.domain.account.gateway;

import com.lemnt.member.domain.account.model.*;
import java.util.List;

public interface MemberAdminQueryGateway {
    List<Group> groups(MemberAdminFilter filter);

    long groupsCount(MemberAdminFilter filter);

    List<Tag> tags(MemberAdminFilter filter);

    long tagsCount(MemberAdminFilter filter);

    List<AssetRecord> assets(MemberAdminFilter filter);

    long assetsCount(MemberAdminFilter filter);

    AssetRecord assetsDetail(Long id);

    List<LevelRecord> levels(MemberAdminFilter filter);

    long levelsCount(MemberAdminFilter filter);

    LevelRecord levelsDetail(Long id);

    List<SignRecord> signs(MemberAdminFilter filter);

    long signsCount(MemberAdminFilter filter);
}
