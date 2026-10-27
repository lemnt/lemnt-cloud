package com.lemnt.member.domain.user.gateway;

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
}
