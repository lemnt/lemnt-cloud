package com.lemnt.mall.infrastructure.catalog.mapper;

import com.lemnt.mall.domain.catalog.model.CatalogFilter;
import com.lemnt.mall.domain.store.model.StoreFilter;
import com.lemnt.mall.infrastructure.catalog.dataobject.SpuDO;
import com.lemnt.mall.infrastructure.store.dataobject.StoreProductDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** Spu 表数据访问接口。 */
@Mapper
public interface SpuMapper extends BaseMapper<SpuDO> {
    String StoreCatalog_JOINS = " FROM mall_spu s JOIN mall_category c ON c.id=s.category_id LEFT JOIN mall_category"
            + " parent ON parent.id=c.parent_id JOIN mall_brand b ON b.id=s.brand_id ";
    String StoreCatalog_VISIBLE = " s.status='ON_SHELF' AND c.status='ENABLED' AND (c.parent_id IS NULL OR"
            + " parent.status='ENABLED') AND b.status='ENABLED' AND EXISTS(SELECT 1 FROM"
            + " mall_product p WHERE p.spu_id=s.id AND p.status='ON_SHELF' AND p.price>0) ";
    String StoreCatalog_FILTER = " <if test='categoryId != null'>AND (c.id=#{categoryId} OR"
            + " c.parent_id=#{categoryId})</if><if test='name != null'>AND s.name LIKE"
            + " CONCAT('%',#{name},'%')</if> ";
    String PromotionCatalog_IDS = "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>";

    /**
     * 插入商品SPU。
     */
    default int insertSpu(SpuDO row) {
        return this.insert(row);
    }

    /**
     * 商品SPU。
     */
    default SpuDO spu(Long id) {
        return this.selectOne(Wrappers.<SpuDO>lambdaQuery()
                .eq(SpuDO::getId, id));
    }

    /**
     * 锁定商品SPU。
     */
    @Select("SELECT * FROM mall_spu WHERE id=#{id} FOR UPDATE")
    SpuDO lockSpu(Long id);

    /**
     * 更新商品SPU。
     */
    default int updateSpu(SpuDO row) {
        return this.update(null, Wrappers.<SpuDO>lambdaUpdate()
                .set(SpuDO::getName, row.getName())
                .set(SpuDO::getCategoryId, row.getCategoryId())
                .set(SpuDO::getBrandId, row.getBrandId())
                .set(SpuDO::getDescription, row.getDescription())
                .set(SpuDO::getImage, row.getImage())
                .eq(SpuDO::getId, row.getId()));
    }

    /**
     * 上架状态。
     */
    default int shelf(Long id, String status) {
        return this.update(null, Wrappers.<SpuDO>lambdaUpdate()
                .set(SpuDO::getStatus, status)
                .eq(SpuDO::getId, id));
    }

    /**
     * 转换分页查询或分页响应。
     */
    default List<SpuDO> page(CatalogFilter filter) {
        return this.selectPage(
                new Page<>((long) filter.offset() / filter.limit() + 1, filter.limit(), false),
                Wrappers.<SpuDO>lambdaQuery()
                .like(filter.name() != null, SpuDO::getName, filter.name())
                .eq(filter.status() != null, SpuDO::getStatus, filter.status())
                .orderByDesc(SpuDO::getId)).getRecords();
    }

    /**
     * 数量。
     */
    default long catalogCount(CatalogFilter filter) {
        return this.selectCount(Wrappers.<SpuDO>lambdaQuery()
                .like(filter.name() != null, SpuDO::getName, filter.name())
                .eq(filter.status() != null, SpuDO::getStatus, filter.status()));
    }

    /**
     * 查询商品。
     */
    @Select(
            "<script>SELECT s.id,s.name,s.category_id,s.brand_id,s.image,(SELECT MIN(p.price) FROM"
            + " mall_product p WHERE p.spu_id=s.id AND p.status='ON_SHELF' AND p.price>0) AS"
            + " min_price,(SELECT MAX(p.price) FROM mall_product p WHERE p.spu_id=s.id AND"
            + " p.status='ON_SHELF' AND p.price>0) AS max_price,EXISTS(SELECT 1 FROM"
            + " mall_product p WHERE p.spu_id=s.id AND p.status='ON_SHELF' AND p.price>0 AND"
            + " p.stock>0) AS available"
            + StoreCatalog_JOINS
            + " WHERE "
            + StoreCatalog_VISIBLE
            + StoreCatalog_FILTER
            + " ORDER BY s.id DESC LIMIT #{limit} OFFSET #{offset}</script>")
    List<StoreProductDO> products(StoreFilter filter);

    /**
     * 数量。
     */
    @Select("<script>SELECT COUNT(*)" + StoreCatalog_JOINS + " WHERE " + StoreCatalog_VISIBLE + StoreCatalog_FILTER + "</script>")
    long storeCatalogCount(StoreFilter filter);

    /**
     * 可见。
     */
    @Select("SELECT s.id" + StoreCatalog_JOINS + " WHERE s.id=#{id} AND " + StoreCatalog_VISIBLE)
    Long visible(Long id);

    /**
     * 已有商品SPU编号集合。
     */
    @Select("<script>SELECT id FROM mall_spu WHERE id IN " + PromotionCatalog_IDS + " ORDER BY id</script>")
    List<Long> existingSpuIds(@Param("ids") List<Long> ids);
}
