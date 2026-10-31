package com.lemnt.member.infrastructure.user.converter;

import com.lemnt.member.domain.user.model.Group;
import com.lemnt.member.infrastructure.user.dataobject.GroupDO;

/**
 * 分组转换器。
 */
public final class GroupConverter {
    private GroupConverter() {}

    /**
     * 转换为领域对象。
     */
    public static Group toEntity(GroupDO row) {
        return row == null
                ? null
                : new Group(row.getId(), row.getName(), row.getRemark(), row.getStatus(), row.getCreateTime());
    }

    /**
     * 转换为持久化数据对象。
     */
    public static GroupDO toDO(Group value) {
        var row = new GroupDO();
        row.setId(value.id());
        row.setName(value.name());
        row.setRemark(value.remark());
        row.setStatus(value.status());
        row.setCreateTime(value.createTime());
        return row;
    }
}
