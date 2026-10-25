package com.lemnt.member.infrastructure.account.converter;

import com.lemnt.member.domain.account.model.Group;
import com.lemnt.member.infrastructure.account.dataobject.GroupDO;

public final class GroupConverter {
    private GroupConverter() {}

    public static Group domain(GroupDO row) {
        return row == null
                ? null
                : new Group(row.getId(), row.getName(), row.getRemark(), row.getStatus());
    }

    public static GroupDO row(Group value) {
        var row = new GroupDO();
        row.setId(value.id());
        row.setName(value.name());
        row.setRemark(value.remark());
        row.setStatus(value.status());
        return row;
    }
}
