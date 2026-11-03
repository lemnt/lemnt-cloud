package com.lemnt.member.infrastructure.user.gateway;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.domain.user.gateway.MemberTagGateway;
import com.lemnt.member.domain.user.model.Tag;
import com.lemnt.member.domain.user.model.TagPageFilter;
import com.lemnt.member.infrastructure.user.converter.TagConverter;
import com.lemnt.member.infrastructure.user.dataobject.TagDO;
import com.lemnt.member.infrastructure.user.mapper.MemberTagMapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
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
        return memberTagMapper
                .selectList(Wrappers.<TagDO>lambdaQuery().orderByAsc(TagDO::getId))
                .stream()
                .map(TagConverter::toEntity)
                .toList();
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
        return tagsPageRow(f).stream().map(TagConverter::toEntity).toList();
    }

    /**
     * 查询标签数量。
     */
    @Override
    public long tagsCount(TagPageFilter f) {
        return tagsCountRow(f);
    }

    /** 批量关联返显，避免逐会员查询。 */
    @Override
    public List<Tag> tagsByIds(Collection<Long> ids) {
        if (ids.isEmpty()) return List.of();
        return memberTagMapper
                .selectList(Wrappers.<TagDO>lambdaQuery().in(TagDO::getId, ids))
                .stream()
                .map(TagConverter::toEntity)
                .toList();
    }

    /** 分页和总数共用条件。 */
    private List<TagDO> tagsPageRow(TagPageFilter f) {
        return memberTagMapper
                .selectPage(
                        new Page<>((long) f.offset() / f.limit() + 1, f.limit(), false),
                        conditions(f).orderByDesc(TagDO::getId))
                .getRecords();
    }

    /** 读取筛选后的总数。 */
    private long tagsCountRow(TagPageFilter f) {
        return memberTagMapper.selectCount(conditions(f));
    }

    /** 分页与计数复用同一组类型安全条件。 */
    private LambdaQueryWrapper<TagDO> conditions(TagPageFilter f) {
        return Wrappers.<TagDO>lambdaQuery()
                .like(f.name() != null, TagDO::getName, f.name())
                .ge(f.startTime() != null, TagDO::getCreateTime, f.startTime())
                .le(f.endTime() != null, TagDO::getCreateTime, f.endTime());
    }
}
