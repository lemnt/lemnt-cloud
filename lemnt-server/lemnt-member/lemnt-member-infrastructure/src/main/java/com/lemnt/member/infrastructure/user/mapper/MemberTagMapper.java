package com.lemnt.member.infrastructure.user.mapper;

import com.lemnt.member.domain.user.model.TagPageFilter;
import com.lemnt.member.infrastructure.user.dataobject.TagDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** member_tag 表数据访问接口。 */
@Mapper
public interface MemberTagMapper extends BaseMapper<TagDO> {

    /**
     * 查询标签。
     */
    default List<TagDO> tags() {
        return this.selectList(Wrappers.<TagDO>lambdaQuery()
                .orderByAsc(TagDO::getId));
    }

    /**
     * 标签。
     */
    default TagDO tag(Long id) {
        return this.selectOne(Wrappers.<TagDO>lambdaQuery()
                .eq(TagDO::getId, id));
    }

    /**
     * 标签插入。
     */
    default int tagInsert(TagDO value) {
        return this.insert(value);
    }

    /**
     * 标签更新。
     */
    default int tagUpdate(TagDO value) {
        return this.update(null, Wrappers.<TagDO>lambdaUpdate()
                .set(TagDO::getName, value.getName())
                .set(TagDO::getRemark, value.getRemark())
                .eq(TagDO::getId, value.getId()));
    }

    /**
     * 标签删除。
     */
    @Delete(
            "DELETE FROM member_tag WHERE id=#{id} AND NOT EXISTS(SELECT 1 FROM member_tag_link"
            + " WHERE tag_id=#{id})")
    int tagDelete(Long id);

    /**
     * 查询标签。
     */
    default List<TagDO> tagsPage(TagPageFilter f) {
        return this.selectPage(new Page<>((long) f.offset() / f.limit() + 1, f.limit(), false),
                Wrappers.<TagDO>lambdaQuery()
                .like(f.name() != null, TagDO::getName, f.name())
                .orderByDesc(TagDO::getId)).getRecords();
    }

    /**
     * 查询标签数量。
     */
    default long tagsCount(TagPageFilter f) {
        return this.selectCount(Wrappers.<TagDO>lambdaQuery()
                .like(f.name() != null, TagDO::getName, f.name()));
    }
}
