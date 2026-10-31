package com.lemnt.member.domain.user.gateway;

import java.util.Collection;

import com.lemnt.member.domain.user.model.Group;
import com.lemnt.member.domain.user.model.GroupPageFilter;

import java.util.List;

/** member_group 表领域存储网关。 */
public interface MemberGroupGateway {

    /**
     * 查询分组。
     */
    List<Group> groups();

    /**
     * 分组。
     */
    Group group(Long id);

    /**
     * 分组保存。
     */
    Long groupSave(Group value);

    /**
     * 分组删除。
     */
    boolean groupDelete(Long id);

    /**
     * 查询分组。
     */
    List<Group> groups(GroupPageFilter filter);

    /**
     * 查询分组数量。
     */
    long groupsCount(GroupPageFilter filter);
    /** 按真实关联ID批量读取，空集合不查表。 */
    List<Group> groupsByIds(Collection<Long> ids);
}
