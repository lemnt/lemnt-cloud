package com.lemnt.member.infrastructure.account.converter;

import com.lemnt.member.domain.account.model.Tag;
import com.lemnt.member.infrastructure.account.dataobject.TagDO;

public final class TagConverter {
    private TagConverter() {}

    public static Tag domain(TagDO row) {
        return row == null ? null : new Tag(row.getId(), row.getName(), row.getRemark());
    }

    public static TagDO row(Tag value) {
        var row = new TagDO();
        row.setId(value.id());
        row.setName(value.name());
        row.setRemark(value.remark());
        return row;
    }
}
