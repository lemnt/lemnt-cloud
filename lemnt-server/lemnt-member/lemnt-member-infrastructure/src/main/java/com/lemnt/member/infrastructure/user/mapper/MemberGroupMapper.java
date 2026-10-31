package com.lemnt.member.infrastructure.user.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lemnt.member.domain.user.model.GroupPageFilter;
import com.lemnt.member.infrastructure.user.dataobject.GroupDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/** Group表简单CRUD复用BaseMapper，跨表删除在XML。 */
@Mapper
public interface MemberGroupMapper extends BaseMapper<GroupDO> {
    /** 原子删除没有会员使用的记录。 */
    int groupDelete(@Param("id") Long id);
    /** 分页和总数共用条件。 */
    default List<GroupDO> groupsPage(GroupPageFilter f) {
        return selectPage(new Page<>((long) f.offset() / f.limit() + 1, f.limit(), false),
                conditions(f).orderByDesc(GroupDO::getId)).getRecords();
    }
    /** 读取筛选后的总数。 */
    default long groupsCount(GroupPageFilter f) { return selectCount(conditions(f)); }
    private LambdaQueryWrapper<GroupDO> conditions(GroupPageFilter f) {
        return Wrappers.<GroupDO>lambdaQuery().like(f.name() != null, GroupDO::getName, f.name())
                .eq(f.status() != null, GroupDO::getStatus, f.status())
                .ge(f.startTime() != null, GroupDO::getCreateTime, f.startTime())
                .le(f.endTime() != null, GroupDO::getCreateTime, f.endTime());
    }
}
