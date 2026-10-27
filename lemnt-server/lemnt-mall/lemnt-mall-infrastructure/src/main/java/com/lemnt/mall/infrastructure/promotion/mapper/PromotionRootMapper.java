package com.lemnt.mall.infrastructure.promotion.mapper;

import com.lemnt.mall.domain.promotion.model.PromotionFilter;
import com.lemnt.mall.infrastructure.promotion.dataobject.PromotionRootDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/** PromotionRoot 表数据访问接口。 */
@Mapper
public interface PromotionRootMapper extends BaseMapper<PromotionRootDO> {

    /**
     * 转换分页查询或分页响应。
     */
    default List<PromotionRootDO> page(PromotionFilter filter) {
        return this.selectPage(
                new Page<>((long) filter.offset() / filter.limit() + 1, filter.limit(), false),
                Wrappers.<PromotionRootDO>lambdaQuery()
                .eq(filter.state() != null, PromotionRootDO::getState, filter.state())
                .eq(filter.code() != null, PromotionRootDO::getCode, filter.code())
                .orderByDesc(PromotionRootDO::getId)).getRecords();
    }

    /**
     * 数量。
     */
    default long count(PromotionFilter filter) {
        return this.selectCount(Wrappers.<PromotionRootDO>lambdaQuery()
                .eq(filter.state() != null, PromotionRootDO::getState, filter.state())
                .eq(filter.code() != null, PromotionRootDO::getCode, filter.code()));
    }

    /**
     * 当前时间毫秒时间戳。
     */
    @Select("SELECT UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000.0")
    long nowEpochMs();

    /**
     * 插入根节点。
     */
    @Insert(
            "INSERT INTO mall_promotion_root(code,draft_json,created_by,updated_by)"
            + " VALUES(#{row.code},#{row.draftJson},#{operator},#{operator})")
    @Options(useGeneratedKeys = true, keyProperty = "row.id")
    int insertRoot(@Param("row") PromotionRootDO row, @Param("operator") long operator);

    /**
     * 根节点。
     */
    default PromotionRootDO root(long id) {
        return this.selectOne(Wrappers.<PromotionRootDO>lambdaQuery()
                .eq(PromotionRootDO::getId, id));
    }

    /**
     * 锁定根节点。
     */
    @Select("SELECT * FROM mall_promotion_root WHERE id=#{id} FOR UPDATE")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    PromotionRootDO lockRoot(long id);

    /**
     * 保存根节点。
     */
    @Update(
            "UPDATE mall_promotion_root SET"
            + " state=#{row.state},revision=#{row.revision},active_version=#{row.activeVersion},last_version=#{row.lastVersion},draft_json=#{row.draftJson},updated_by=#{operator}"
            + " WHERE id=#{row.id} AND revision=#{expected}")
    int saveRoot(
            @Param("row") PromotionRootDO row,
            @Param("expected") long expected,
            @Param("operator") long operator);
}
