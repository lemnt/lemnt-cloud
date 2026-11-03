package com.lemnt.member.infrastructure.user.gateway;

import com.lemnt.member.domain.user.gateway.MemberTagLinkGateway;
import com.lemnt.member.infrastructure.user.dataobject.MemberTagLinkDO;
import com.lemnt.member.infrastructure.user.mapper.MemberTagLinkMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        return memberTagsRow(memberId);
    }

    /**
     * 读取或替换会员关联的标签编号。
     */
    @Override
    public void memberTags(Long memberId, List<Long> tags) {
        clearTagsRow(memberId);
        for (Long tag : tags) {
            addTagRow(memberId, tag);
        }
    }

    /** 按会员和标签编号排序，返回稳定的当前绑定。 */
    @Override
    public Map<Long, List<Long>> memberTagsBatch(Collection<Long> memberIds) {
        if (memberIds.isEmpty()) return java.util.Map.of();
        return memberTagLinkMapper
                .selectList(
                        Wrappers.<MemberTagLinkDO>lambdaQuery()
                                .in(MemberTagLinkDO::getMemberId, memberIds)
                                .orderByAsc(MemberTagLinkDO::getMemberId)
                                .orderByAsc(MemberTagLinkDO::getTagId))
                .stream()
                .collect(
                        Collectors.groupingBy(
                                MemberTagLinkDO::getMemberId,
                                Collectors.mapping(
                                        MemberTagLinkDO::getTagId, Collectors.toList())));
    }

    /**
     * 读取或替换会员关联的标签编号。
     */
    private List<Long> memberTagsRow(Long memberId) {
        return memberTagLinkMapper
                .selectList(
                        Wrappers.<MemberTagLinkDO>lambdaQuery()
                                .select(MemberTagLinkDO::getTagId)
                                .eq(MemberTagLinkDO::getMemberId, memberId)
                                .orderByAsc(MemberTagLinkDO::getTagId))
                .stream()
                .map(MemberTagLinkDO::getTagId)
                .toList();
    }

    /**
     * 清除查询标签。
     */
    private int clearTagsRow(Long memberId) {
        return memberTagLinkMapper.delete(
                Wrappers.<MemberTagLinkDO>lambdaQuery().eq(MemberTagLinkDO::getMemberId, memberId));
    }

    /**
     * 新增标签。
     */
    private int addTagRow(Long memberId, Long tagId) {
        return memberTagLinkMapper.insert(
                new MemberTagLinkDO().setMemberId(memberId).setTagId(tagId));
    }
}
