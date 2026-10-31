package com.lemnt.member.infrastructure.user.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lemnt.member.domain.user.model.TagPageFilter;
import com.lemnt.member.infrastructure.user.dataobject.TagDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/** Tag表简单CRUD复用BaseMapper，跨表删除在XML。 */
@Mapper
public interface MemberTagMapper extends BaseMapper<TagDO> {
    /** 原子删除没有会员使用的记录。 */
    int tagDelete(@Param("id") Long id);
    /** 分页和总数共用条件。 */
    default List<TagDO> tagsPage(TagPageFilter f) {
        return selectPage(new Page<>((long) f.offset() / f.limit() + 1, f.limit(), false),
                conditions(f).orderByDesc(TagDO::getId)).getRecords();
    }
    /** 读取筛选后的总数。 */
    default long tagsCount(TagPageFilter f) { return selectCount(conditions(f)); }
    private LambdaQueryWrapper<TagDO> conditions(TagPageFilter f) {
        return Wrappers.<TagDO>lambdaQuery().like(f.name() != null, TagDO::getName, f.name())

                .ge(f.startTime() != null, TagDO::getCreateTime, f.startTime())
                .le(f.endTime() != null, TagDO::getCreateTime, f.endTime());
    }
}
