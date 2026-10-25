package com.lemnt.mall.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lemnt.mall.infrastructure.dataobject.ProductDO;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ProductMapper extends BaseMapper<ProductDO> {
    String SELLABLE =
            "(spu_id IS NULL OR EXISTS (SELECT 1 FROM mall_spu s JOIN mall_category c ON"
                + " c.id=s.category_id JOIN mall_brand b ON b.id=s.brand_id LEFT JOIN mall_category"
                + " parent ON parent.id=c.parent_id WHERE s.id=mall_product.spu_id AND"
                + " s.status='ON_SHELF' AND c.status='ENABLED' AND b.status='ENABLED' AND"
                + " (c.parent_id IS NULL OR parent.status='ENABLED')))";

    @Select("SELECT * FROM mall_product WHERE id=#{id} FOR UPDATE")
    ProductDO lockById(Long id);

    @Select(
            "<script>SELECT * FROM mall_product WHERE status='ON_SHELF' AND "
                    + SELLABLE
                    + " AND id IN <foreach collection='ids' item='id' open='(' separator=','"
                    + " close=')'>#{id}</foreach></script>")
    java.util.List<ProductDO> findSellableByIds(@Param("ids") java.util.List<Long> ids);

    @Select("SELECT * FROM mall_product WHERE id=#{id} AND status='ON_SHELF' AND " + SELLABLE)
    ProductDO findSellableById(Long id);

    @Update(
            "UPDATE mall_product SET stock=stock-#{quantity},update_time=CURRENT_TIMESTAMP "
                    + "WHERE id=#{id} AND status='ON_SHELF' AND stock>=#{quantity} AND "
                    + SELLABLE)
    int reserveStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Update(
            "UPDATE mall_product SET stock=stock+#{quantity},update_time=CURRENT_TIMESTAMP WHERE"
                    + " id=#{id}")
    int releaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}
