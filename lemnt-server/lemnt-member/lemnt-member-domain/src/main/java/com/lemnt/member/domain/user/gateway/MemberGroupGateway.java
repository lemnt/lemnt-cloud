package com.lemnt.member.domain.user.gateway;

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
}
