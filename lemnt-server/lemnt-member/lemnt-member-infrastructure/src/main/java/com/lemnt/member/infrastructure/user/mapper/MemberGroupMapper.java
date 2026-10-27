package com.lemnt.member.infrastructure.user.mapper;

import com.lemnt.member.domain.user.model.GroupPageFilter;
import com.lemnt.member.infrastructure.user.dataobject.GroupDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** member_group 表数据访问接口。 */
@Mapper
public interface MemberGroupMapper extends BaseMapper<GroupDO> {

    /**
     * 查询分组。
     */
    default List<GroupDO> groups() {
        return this.selectList(Wrappers.<GroupDO>lambdaQuery()
                .orderByAsc(GroupDO::getId));
    }

    /**
     * 分组。
     */
    default GroupDO group(Long id) {
        return this.selectOne(Wrappers.<GroupDO>lambdaQuery()
                .eq(GroupDO::getId, id));
    }

    /**
     * 分组插入。
     */
    default int groupInsert(GroupDO value) {
        return this.insert(value);
    }

    /**
     * 分组更新。
     */
    default int groupUpdate(GroupDO value) {
        return this.update(null, Wrappers.<GroupDO>lambdaUpdate()
                .set(GroupDO::getName, value.getName())
                .set(GroupDO::getRemark, value.getRemark())
                .set(GroupDO::getStatus, value.getStatus())
                .eq(GroupDO::getId, value.getId()));
    }

    /**
     * 分组删除。
     */
    @Delete(
            "DELETE FROM member_group WHERE id=#{id} AND NOT EXISTS(SELECT 1 FROM member_user WHERE"
            + " group_id=#{id})")
    int groupDelete(Long id);

    /**
     * 查询分组。
     */
    default List<GroupDO> groupsPage(GroupPageFilter f) {
        return this.selectPage(new Page<>((long) f.offset() / f.limit() + 1, f.limit(), false),
                Wrappers.<GroupDO>lambdaQuery()
                .like(f.name() != null, GroupDO::getName, f.name())
                .eq(f.status() != null, GroupDO::getStatus, f.status())
                .orderByDesc(GroupDO::getId)).getRecords();
    }

    /**
     * 查询分组数量。
     */
    default long groupsCount(GroupPageFilter f) {
        return this.selectCount(Wrappers.<GroupDO>lambdaQuery()
                .like(f.name() != null, GroupDO::getName, f.name())
                .eq(f.status() != null, GroupDO::getStatus, f.status()));
    }
}
