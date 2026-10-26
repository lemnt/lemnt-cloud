package com.lemnt.mall.infrastructure.promotion.mapper;

import com.lemnt.mall.infrastructure.promotion.dataobject.PricingFactsDO;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PromotionCatalogMapper {
    String IDS =
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>";

    @Select(
            "<script>SELECT p.id AS sku_id,p.spu_id,p.product_code,p.name,p.price,p.stock FROM"
                + " mall_product p JOIN mall_spu s ON s.id=p.spu_id JOIN mall_category c ON"
                + " c.id=s.category_id LEFT JOIN mall_category parent ON parent.id=c.parent_id JOIN"
                + " mall_brand b ON b.id=s.brand_id WHERE p.id IN "
                    + IDS
                    + " AND p.status='ON_SHELF' AND p.price>0 AND s.status='ON_SHELF' AND"
                    + " c.status='ENABLED' AND (c.parent_id IS NULL OR parent.status='ENABLED') AND"
                    + " b.status='ENABLED' ORDER BY p.id</script>")
    List<PricingFactsDO> facts(@Param("ids") List<Long> ids);

    @Select("<script>SELECT id FROM mall_product WHERE id IN " + IDS + " ORDER BY id</script>")
    List<Long> existingSkuIds(@Param("ids") List<Long> ids);

    @Select("<script>SELECT id FROM mall_spu WHERE id IN " + IDS + " ORDER BY id</script>")
    List<Long> existingSpuIds(@Param("ids") List<Long> ids);
}
