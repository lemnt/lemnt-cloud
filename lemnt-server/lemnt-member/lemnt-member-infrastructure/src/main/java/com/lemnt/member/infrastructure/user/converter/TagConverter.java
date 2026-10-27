package com.lemnt.member.infrastructure.user.converter;

import com.lemnt.member.domain.user.model.Tag;
import com.lemnt.member.infrastructure.user.dataobject.TagDO;

/**
 * 标签转换器。
 */
public final class TagConverter {
    private TagConverter() {}

    /**
     * 转换为领域对象。
     */
    public static Tag toEntity(TagDO row) {
        return row == null ? null : new Tag(row.getId(), row.getName(), row.getRemark());
    }

    /**
     * 转换为持久化数据对象。
     */
    public static TagDO toDO(Tag value) {
        var row = new TagDO();
        row.setId(value.id());
        row.setName(value.name());
        row.setRemark(value.remark());
        return row;
    }
}
