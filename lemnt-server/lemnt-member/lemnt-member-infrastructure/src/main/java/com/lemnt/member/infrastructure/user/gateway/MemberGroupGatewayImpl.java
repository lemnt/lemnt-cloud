package com.lemnt.member.infrastructure.user.gateway;

import java.util.Collection;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.domain.user.gateway.MemberGroupGateway;
import com.lemnt.member.domain.user.model.Group;
import com.lemnt.member.domain.user.model.GroupPageFilter;
import com.lemnt.member.infrastructure.user.converter.GroupConverter;
import com.lemnt.member.infrastructure.user.mapper.MemberGroupMapper;
import com.lemnt.member.infrastructure.user.dataobject.GroupDO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.dao.DuplicateKeyException;

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
        return memberGroupMapper.selectList(Wrappers.<GroupDO>lambdaQuery().orderByAsc(GroupDO::getId)).stream().map(GroupConverter::toEntity).toList();
    }

    /**
     * 分组。
     */
    @Override
    public Group group(Long id) {
        return GroupConverter.toEntity(memberGroupMapper.selectById(id));
    }

    /**
     * 分组保存。
     */
    @Override
    public Long groupSave(Group value) {
        var row = GroupConverter.toDO(value);
        try {
            if (value.id() == null) {
                memberGroupMapper.insert(row);
            } else if (memberGroupMapper.updateById(row) != 1) {
                throw new ServiceException("记录不存在", 404);
            }
        } catch (DuplicateKeyException exception) {
            throw new ServiceException("分组名称已存在", 409);
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
    /** 批量关联返显，避免逐会员查询。 */
    @Override
    public List<Group> groupsByIds(Collection<Long> ids) {
        if (ids.isEmpty()) return List.of();
        return memberGroupMapper.selectList(Wrappers.<GroupDO>lambdaQuery().in(GroupDO::getId, ids))
                .stream().map(GroupConverter::toEntity).toList();
    }
}
