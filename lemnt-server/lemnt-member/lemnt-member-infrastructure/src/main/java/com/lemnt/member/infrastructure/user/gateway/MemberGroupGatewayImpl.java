package com.lemnt.member.infrastructure.user.gateway;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.domain.user.gateway.MemberGroupGateway;
import com.lemnt.member.domain.user.model.Group;
import com.lemnt.member.domain.user.model.GroupPageFilter;
import com.lemnt.member.infrastructure.user.converter.GroupConverter;
import com.lemnt.member.infrastructure.user.mapper.MemberGroupMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** member_group 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class MemberGroupGatewayImpl implements MemberGroupGateway {

    private final MemberGroupMapper memberGroupMapper;

    /**
     * 查询分组。
     */
    @Override
    public List<Group> groups() {
        return memberGroupMapper.groups().stream().map(GroupConverter::toEntity).toList();
    }

    /**
     * 分组。
     */
    @Override
    public Group group(Long id) {
        return GroupConverter.toEntity(memberGroupMapper.group(id));
    }

    /**
     * 分组保存。
     */
    @Override
    public Long groupSave(Group value) {
        var row = GroupConverter.toDO(value);
        if (value.id() == null) {
            memberGroupMapper.groupInsert(row);
        }
        else if (memberGroupMapper.groupUpdate(row) != 1) {
            throw new ServiceException("记录不存在", 409);
        }
        return row.getId();
    }

    /**
     * 分组删除。
     */
    @Override
    public boolean groupDelete(Long id) {
        return memberGroupMapper.groupDelete(id) == 1;
    }

    /**
     * 查询分组。
     */
    @Override
    public List<Group> groups(GroupPageFilter f) {
        return memberGroupMapper.groupsPage(f).stream().map(GroupConverter::toEntity).toList();
    }

    /**
     * 查询分组数量。
     */
    @Override
    public long groupsCount(GroupPageFilter f) {
        return memberGroupMapper.groupsCount(f);
    }
}
