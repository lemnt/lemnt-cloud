package com.lemnt.member.domain.user.gateway;

import com.lemnt.member.domain.user.model.Tag;
import com.lemnt.member.domain.user.model.TagPageFilter;

import java.util.List;

/** member_tag 表领域存储网关。 */
public interface MemberTagGateway {

    /**
     * 查询标签。
     */
    List<Tag> tags();

    /**
     * 标签。
     */
    Tag tag(Long id);

    /**
     * 标签保存。
     */
    Long tagSave(Tag value);

    /**
     * 标签删除。
     */
    boolean tagDelete(Long id);

    /**
     * 查询标签。
     */
    List<Tag> tags(TagPageFilter filter);

    /**
     * 查询标签数量。
     */
    long tagsCount(TagPageFilter filter);
}
