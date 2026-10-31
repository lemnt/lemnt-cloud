package com.lemnt.member.infrastructure.user.gateway;

import java.util.Collection;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.domain.user.gateway.MemberTagGateway;
import com.lemnt.member.domain.user.model.Tag;
import com.lemnt.member.domain.user.model.TagPageFilter;
import com.lemnt.member.infrastructure.user.converter.TagConverter;
import com.lemnt.member.infrastructure.user.mapper.MemberTagMapper;
import com.lemnt.member.infrastructure.user.dataobject.TagDO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.dao.DuplicateKeyException;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** member_tag 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class MemberTagGatewayImpl implements MemberTagGateway {

    private final MemberTagMapper memberTagMapper;

    /**
     * 查询标签。
     */
    @Override
    public List<Tag> tags() {
        return memberTagMapper.selectList(Wrappers.<TagDO>lambdaQuery().orderByAsc(TagDO::getId)).stream().map(TagConverter::toEntity).toList();
    }

    /**
     * 标签。
     */
    @Override
    public Tag tag(Long id) {
        return TagConverter.toEntity(memberTagMapper.selectById(id));
    }

    /**
     * 标签保存。
     */
    @Override
    public Long tagSave(Tag value) {
        var row = TagConverter.toDO(value);
        try {
            if (value.id() == null) {
                memberTagMapper.insert(row);
            } else if (memberTagMapper.updateById(row) != 1) {
                throw new ServiceException("记录不存在", 404);
            }
        } catch (DuplicateKeyException exception) {
            throw new ServiceException("标签名称已存在", 409);
        }
        return row.getId();
    }

    /**
     * 标签删除。
     */
    @Override
    public boolean tagDelete(Long id) {
        return memberTagMapper.tagDelete(id) == 1;
    }

    /**
     * 查询标签。
     */
    @Override
    public List<Tag> tags(TagPageFilter f) {
        return memberTagMapper.tagsPage(f).stream().map(TagConverter::toEntity).toList();
    }

    /**
     * 查询标签数量。
     */
    @Override
    public long tagsCount(TagPageFilter f) {
        return memberTagMapper.tagsCount(f);
    }
    /** 批量关联返显，避免逐会员查询。 */
    @Override
    public List<Tag> tagsByIds(Collection<Long> ids) {
        if (ids.isEmpty()) return List.of();
        return memberTagMapper.selectList(Wrappers.<TagDO>lambdaQuery().in(TagDO::getId, ids))
                .stream().map(TagConverter::toEntity).toList();
    }
}
