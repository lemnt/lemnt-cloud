package com.lemnt.mall.infrastructure.store.mapper;

import com.lemnt.mall.infrastructure.store.dataobject.StoreProductDO;
import com.lemnt.mall.infrastructure.catalog.dataobject.CategoryDO;
import com.lemnt.mall.domain.store.model.StoreFilter;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface StoreCatalogMapper {
    String JOINS =
            " FROM mall_spu s JOIN mall_category c ON c.id=s.category_id LEFT JOIN mall_category"
                + " parent ON parent.id=c.parent_id JOIN mall_brand b ON b.id=s.brand_id ";
    String VISIBLE =
            " s.status='ON_SHELF' AND c.status='ENABLED' AND (c.parent_id IS NULL OR"
                + " parent.status='ENABLED') AND b.status='ENABLED' AND EXISTS(SELECT 1 FROM"
                + " mall_product p WHERE p.spu_id=s.id AND p.status='ON_SHELF' AND p.price>0) ";
    String FILTER =
            " <if test='categoryId != null'>AND (c.id=#{categoryId} OR"
                + " c.parent_id=#{categoryId})</if><if test='name != null'>AND s.name LIKE"
                + " CONCAT('%',#{name},'%')</if> ";

    @Select(
            "<script>SELECT s.id,s.name,s.category_id,s.brand_id,s.image,(SELECT MIN(p.price) FROM"
                + " mall_product p WHERE p.spu_id=s.id AND p.status='ON_SHELF' AND p.price>0) AS"
                + " min_price,(SELECT MAX(p.price) FROM mall_product p WHERE p.spu_id=s.id AND"
                + " p.status='ON_SHELF' AND p.price>0) AS max_price,EXISTS(SELECT 1 FROM"
                + " mall_product p WHERE p.spu_id=s.id AND p.status='ON_SHELF' AND p.price>0 AND"
                + " p.stock>0) AS available"
                    + JOINS
                    + " WHERE "
                    + VISIBLE
                    + FILTER
                    + " ORDER BY s.id DESC LIMIT #{limit} OFFSET #{offset}</script>")
    List<StoreProductDO> products(StoreFilter filter);

    @Select("<script>SELECT COUNT(*)" + JOINS + " WHERE " + VISIBLE + FILTER + "</script>")
    long count(StoreFilter filter);

    @Select("SELECT s.id" + JOINS + " WHERE s.id=#{id} AND " + VISIBLE)
    Long visible(Long id);

    @Select(
            "SELECT c.* FROM mall_category c LEFT JOIN mall_category p ON p.id=c.parent_id WHERE"
                + " c.status='ENABLED' AND (c.parent_id IS NULL OR p.status='ENABLED') ORDER BY"
                + " c.sort,c.id LIMIT 1001")
    List<CategoryDO> categories();
}
