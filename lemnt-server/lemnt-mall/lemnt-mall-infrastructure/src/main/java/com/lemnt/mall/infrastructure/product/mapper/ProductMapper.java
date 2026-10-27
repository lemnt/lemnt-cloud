package com.lemnt.mall.infrastructure.product.mapper;

import com.lemnt.mall.infrastructure.catalog.dataobject.SkuDO;
import com.lemnt.mall.infrastructure.catalog.dataobject.StockAdjustmentDO;
import com.lemnt.mall.infrastructure.product.dataobject.ProductDO;
import com.lemnt.mall.infrastructure.promotion.dataobject.PricingFactsDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/** Product 表数据访问接口。 */
@Mapper
public interface ProductMapper extends BaseMapper<ProductDO> {
    String Catalog_SKU_COLUMNS = "id,spu_id,product_code AS code,name,price,stock,main_image AS"
            + " image,status,variant_key";
    String PromotionCatalog_IDS = "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>";
    String Product_SELLABLE = "(spu_id IS NULL OR EXISTS (SELECT 1 FROM mall_spu s JOIN mall_category c ON"
            + " c.id=s.category_id JOIN mall_brand b ON b.id=s.brand_id LEFT JOIN mall_category"
    + " parent ON parent.id=c.parent_id WHERE s.id=mall_product.spu_id AND"
    + " s.status='ON_SHELF' AND c.status='ENABLED' AND b.status='ENABLED' AND"
            + " (c.parent_id IS NULL OR parent.status='ENABLED')))";

    /**
     * 查询商品SKU。
     */
    @Select("SELECT " + Catalog_SKU_COLUMNS + " FROM mall_product WHERE spu_id=#{id} ORDER BY id")
    List<SkuDO> skus(Long id);

    /**
     * 商品SKU。
     */
    @Select("SELECT " + Catalog_SKU_COLUMNS + " FROM mall_product WHERE id=#{id}")
    SkuDO sku(Long id);

    /**
     * 插入商品SKU。
     */
    @Insert(
            "INSERT INTO"
            + " mall_product(spu_id,product_code,name,price,stock,main_image,status,variant_key)"
            + " VALUES(#{spuId},#{code},#{name},#{price},#{stock},#{image},#{status},#{variantKey})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertSku(SkuDO row);

    /**
     * 更新商品SKU。
     */
    @Update(
            "UPDATE mall_product SET"
            + " name=#{name},price=#{price},main_image=#{image},update_time=CURRENT_TIMESTAMP"
            + " WHERE id=#{id}")
    int updateSku(SkuDO row);

    /**
     * 调整库存。
     */
    @Update(
            "UPDATE mall_product SET stock=stock+#{delta},update_time=CURRENT_TIMESTAMP WHERE"
            + " id=#{productId} AND CAST(stock AS DECIMAL(20,0))+#{delta} BETWEEN 0 AND"
            + " 2147483647 AND CAST(stock AS DECIMAL(20,0))+#{delta}+(SELECT"
            + " COALESCE(SUM(r.quantity),0) FROM mall_stock_reservation r WHERE"
            + " r.product_id=#{productId} AND r.status='RESERVED') <= 2147483647")
    int adjustStock(StockAdjustmentDO row);

    /**
     * 事实。
     */
    @Select(
            "<script>SELECT p.id AS sku_id,p.spu_id,p.product_code,p.name,p.price,p.stock FROM"
            + " mall_product p JOIN mall_spu s ON s.id=p.spu_id JOIN mall_category c ON"
            + " c.id=s.category_id LEFT JOIN mall_category parent ON parent.id=c.parent_id JOIN"
            + " mall_brand b ON b.id=s.brand_id WHERE p.id IN "
            + PromotionCatalog_IDS
            + " AND p.status='ON_SHELF' AND p.price>0 AND s.status='ON_SHELF' AND"
            + " c.status='ENABLED' AND (c.parent_id IS NULL OR parent.status='ENABLED') AND"
            + " b.status='ENABLED' ORDER BY p.id</script>")
    List<PricingFactsDO> facts(@Param("ids") List<Long> ids);

    /**
     * 已有商品SKU编号集合。
     */
    @Select("<script>SELECT id FROM mall_product WHERE id IN " + PromotionCatalog_IDS + " ORDER BY id</script>")
    List<Long> existingSkuIds(@Param("ids") List<Long> ids);

    /**
     * 锁定按编号。
     */
    @Select("SELECT * FROM mall_product WHERE id=#{id} FOR UPDATE")
    ProductDO lockById(Long id);

    /**
     * 查找可售商品按编号集合。
     */
    @Select(
            "<script>SELECT * FROM mall_product WHERE status='ON_SHELF' AND "
            + Product_SELLABLE
            + " AND id IN <foreach collection='ids' item='id' open='(' separator=','"
            + " close=')'>#{id}</foreach></script>")
    List<ProductDO> findSellableByIds(@Param("ids") List<Long> ids);

    /**
     * 查找可售商品按编号。
     */
    @Select("SELECT * FROM mall_product WHERE id=#{id} AND status='ON_SHELF' AND " + Product_SELLABLE)
    ProductDO findSellableById(Long id);

    /**
     * 预留库存。
     */
    @Update(
            "UPDATE mall_product SET stock=stock-#{quantity},update_time=CURRENT_TIMESTAMP "
            + "WHERE id=#{id} AND status='ON_SHELF' AND stock>=#{quantity} AND "
            + Product_SELLABLE)
    int reserveStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 释放库存。
     */
    @Update(
            "UPDATE mall_product SET stock=stock+#{quantity},update_time=CURRENT_TIMESTAMP WHERE"
            + " id=#{id}")
    int releaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}
