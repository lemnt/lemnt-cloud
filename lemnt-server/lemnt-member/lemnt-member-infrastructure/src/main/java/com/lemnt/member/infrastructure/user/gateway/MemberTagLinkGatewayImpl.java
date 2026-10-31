package com.lemnt.member.infrastructure.user.gateway;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import com.lemnt.member.domain.user.gateway.MemberTagLinkGateway;
import com.lemnt.member.infrastructure.user.mapper.MemberTagLinkMapper;
import com.lemnt.member.infrastructure.user.dataobject.MemberTagLinkDO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** member_tag_link 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class MemberTagLinkGatewayImpl implements MemberTagLinkGateway {

    private final MemberTagLinkMapper memberTagLinkMapper;

    /**
     * 读取或替换会员关联的标签编号。
     */
    @Override
    public List<Long> memberTags(Long memberId) {
        return memberTagLinkMapper.memberTags(memberId);
    }

    /**
     * 读取或替换会员关联的标签编号。
     */
    @Override
    public void memberTags(Long memberId, List<Long> tags) {
        memberTagLinkMapper.clearTags(memberId);
        for (Long tag : tags) {
            memberTagLinkMapper.addTag(memberId, tag);
        }
    }
    /** 按会员和标签编号排序，返回稳定的当前绑定。 */
    @Override
    public Map<Long, List<Long>> memberTagsBatch(Collection<Long> memberIds) {
        if (memberIds.isEmpty()) return java.util.Map.of();
        return memberTagLinkMapper.selectList(Wrappers.<MemberTagLinkDO>lambdaQuery()
                .in(MemberTagLinkDO::getMemberId, memberIds)
                .orderByAsc(MemberTagLinkDO::getMemberId).orderByAsc(MemberTagLinkDO::getTagId))
                .stream().collect(Collectors.groupingBy(MemberTagLinkDO::getMemberId,
                        Collectors.mapping(MemberTagLinkDO::getTagId, Collectors.toList())));
    }
}
