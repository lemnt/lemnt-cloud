package com.lemnt.member.domain.user.gateway;

import java.util.Collection;
import java.util.Map;

import java.util.List;

/** member_tag_link 表领域存储网关。 */
public interface MemberTagLinkGateway {

    /**
     * 读取或替换会员关联的标签编号。
     */
    List<Long> memberTags(Long memberId);

    /**
     * 读取或替换会员关联的标签编号。
     */
    void memberTags(Long memberId, List<Long> tags);
    /** 批量读取会员真实标签绑定，无绑定的会员不生成虚构标签。 */
    Map<Long, List<Long>> memberTagsBatch(Collection<Long> memberIds);
}
