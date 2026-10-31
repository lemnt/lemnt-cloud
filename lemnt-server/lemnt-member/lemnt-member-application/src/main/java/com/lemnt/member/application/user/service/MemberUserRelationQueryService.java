package com.lemnt.member.application.user.service;

import com.lemnt.member.domain.growth.model.Level;
import com.lemnt.member.domain.user.model.Group;
import com.lemnt.member.domain.user.model.Tag;

import com.lemnt.member.application.user.dto.MemberDTO;
import com.lemnt.member.application.user.converter.MemberUserDTOConverter;
import com.lemnt.member.domain.growth.gateway.MemberLevelGateway;
import com.lemnt.member.domain.user.gateway.MemberGroupGateway;
import com.lemnt.member.domain.user.gateway.MemberTagGateway;
import com.lemnt.member.domain.user.gateway.MemberTagLinkGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** 后台关联返显按页批量读取，基础/App查询不触发。 */
@Component
@RequiredArgsConstructor
public class MemberUserRelationQueryService {
    private final MemberLevelGateway levels;
    private final MemberGroupGateway groups;
    private final MemberTagGateway tags;
    private final MemberTagLinkGateway links;

    /** 每张关联表最多查一次，名称来自当前真实记录。 */
    public List<MemberDTO> enrich(List<MemberDTO> users) {
        if (users.isEmpty()) return List.of();
        var bindings = links.memberTagsBatch(users.stream().map(MemberDTO::id).toList());
        var levelIds = users.stream().map(MemberDTO::levelId).filter(Objects::nonNull).distinct().toList();
        var groupIds = users.stream().map(MemberDTO::groupId).filter(Objects::nonNull).distinct().toList();
        var tagIds = bindings.values().stream().flatMap(List::stream).distinct().toList();
        var levelNames = levels.levelsByIds(levelIds).stream().collect(Collectors.toMap(
                Level::id, Level::name));
        var groupNames = groups.groupsByIds(groupIds).stream().collect(Collectors.toMap(
                Group::id, Group::name));
        var tagNames = tags.tagsByIds(tagIds).stream().collect(Collectors.toMap(
                Tag::id, Tag::name));
        return users.stream().map(user -> {
            var ids = bindings.getOrDefault(user.id(), List.of());
            return MemberUserDTOConverter.withRelations(user, ids,
                    ids.stream().map(tagNames::get).filter(Objects::nonNull).toList(),
                    levelNames.get(user.levelId()), groupNames.get(user.groupId()));
        }).toList();
    }
}
