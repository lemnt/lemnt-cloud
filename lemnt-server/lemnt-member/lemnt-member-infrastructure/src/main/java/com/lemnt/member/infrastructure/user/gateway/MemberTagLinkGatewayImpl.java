package com.lemnt.member.infrastructure.user.gateway;

import com.lemnt.member.domain.user.gateway.MemberTagLinkGateway;
import com.lemnt.member.infrastructure.user.mapper.MemberTagLinkMapper;

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
}
