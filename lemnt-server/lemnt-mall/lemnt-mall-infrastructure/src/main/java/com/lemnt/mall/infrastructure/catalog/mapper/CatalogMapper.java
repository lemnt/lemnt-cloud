package com.lemnt.mall.infrastructure.catalog.mapper;

import com.lemnt.mall.infrastructure.catalog.dataobject.*;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CatalogMapper {
    @Select("SELECT * FROM mall_category ORDER BY sort,id LIMIT 1001")
    List<CategoryDO> categories();

    @Select("SELECT * FROM mall_category WHERE id=#{id}")
    CategoryDO category(Long id);

    @Insert(
            "INSERT INTO mall_category(parent_id,name,pic_url,sort,status)"
                    + " VALUES(#{parentId},#{name},#{picUrl},#{sort},#{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertCategory(CategoryDO row);

    @Update(
            "UPDATE mall_category SET name=#{name},pic_url=#{picUrl},sort=#{sort},status=#{status}"
                    + " WHERE id=#{id}")
    int updateCategory(CategoryDO row);

    @Delete("DELETE FROM mall_category WHERE id=#{id}")
    int deleteCategory(Long id);

    @Select("SELECT * FROM mall_brand ORDER BY sort,id LIMIT 1001")
    List<BrandDO> brands();

    @Select("SELECT * FROM mall_brand WHERE id=#{id}")
    BrandDO brand(Long id);

    @Insert(
            "INSERT INTO mall_brand(name,pic_url,description,sort,status)"
                    + " VALUES(#{name},#{picUrl},#{description},#{sort},#{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertBrand(BrandDO row);

    @Update(
            "UPDATE mall_brand SET"
                + " name=#{name},pic_url=#{picUrl},description=#{description},sort=#{sort},status=#{status}"
                + " WHERE id=#{id}")
    int updateBrand(BrandDO row);

    @Delete("DELETE FROM mall_brand WHERE id=#{id}")
    int deleteBrand(Long id);

    @Insert(
            "INSERT INTO mall_spu(code,name,category_id,brand_id,description,image,status) "
                + "VALUES(#{code},#{name},#{categoryId},#{brandId},#{description},#{image},#{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertSpu(SpuDO row);

    @Select("SELECT * FROM mall_spu WHERE id=#{id}")
    SpuDO spu(Long id);

    @Select("SELECT * FROM mall_spu WHERE id=#{id} FOR UPDATE")
    SpuDO lockSpu(Long id);

    @Update(
            "UPDATE mall_spu SET"
                + " name=#{name},category_id=#{categoryId},brand_id=#{brandId},description=#{description},image=#{image}"
                + " WHERE id=#{id}")
    int updateSpu(SpuDO row);

    @Update("UPDATE mall_spu SET status=#{status} WHERE id=#{id}")
    int shelf(@Param("id") Long id, @Param("status") String status);

    String FILTER =
            "<where><if test='name != null'>name LIKE CONCAT('%',#{name},'%')</if>"
                    + "<if test='status != null'>AND status=#{status}</if></where>";

    @Select(
            "<script>SELECT * FROM mall_spu "
                    + FILTER
                    + " ORDER BY id DESC LIMIT #{limit} OFFSET #{offset}</script>")
    List<SpuDO> page(com.lemnt.mall.domain.catalog.model.CatalogFilter filter);

    @Select("<script>SELECT COUNT(*) FROM mall_spu " + FILTER + "</script>")
    long count(com.lemnt.mall.domain.catalog.model.CatalogFilter filter);

    String SKU_COLUMNS =
            "id,spu_id,product_code AS code,name,price,stock,main_image AS"
                    + " image,status,variant_key";

    @Select("SELECT " + SKU_COLUMNS + " FROM mall_product WHERE spu_id=#{id} ORDER BY id")
    List<SkuDO> skus(Long id);

    @Select("SELECT " + SKU_COLUMNS + " FROM mall_product WHERE id=#{id}")
    SkuDO sku(Long id);

    @Insert(
            "INSERT INTO"
                + " mall_product(spu_id,product_code,name,price,stock,main_image,status,variant_key)"
                + " VALUES(#{spuId},#{code},#{name},#{price},#{stock},#{image},#{status},#{variantKey})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertSku(SkuDO row);

    @Update(
            "UPDATE mall_product SET"
                + " name=#{name},price=#{price},main_image=#{image},update_time=CURRENT_TIMESTAMP"
                + " WHERE id=#{id}")
    int updateSku(SkuDO row);

    @Insert(
            "INSERT INTO mall_sku_property(product_id,property_name,property_value)"
                    + " VALUES(#{productId},#{propertyName},#{propertyValue})")
    int insertProperty(SkuPropertyDO row);

    @Select(
            "SELECT p.* FROM mall_sku_property p JOIN mall_product s ON s.id=p.product_id WHERE"
                    + " s.spu_id=#{id} ORDER BY p.property_name")
    List<SkuPropertyDO> propertiesBySpu(Long id);

    @Select("SELECT * FROM mall_sku_property WHERE product_id=#{id} ORDER BY property_name")
    List<SkuPropertyDO> properties(Long id);

    @Select("SELECT * FROM mall_stock_adjustment WHERE request_no=#{requestNo}")
    StockAdjustmentDO adjustment(String requestNo);

    @Insert(
            "INSERT INTO mall_stock_adjustment(request_no,product_id,delta,reason)"
                    + " VALUES(#{requestNo},#{productId},#{delta},#{reason})")
    int insertAdjustment(StockAdjustmentDO row);

    @Update(
            "UPDATE mall_product SET stock=stock+#{delta},update_time=CURRENT_TIMESTAMP WHERE"
                    + " id=#{productId} AND CAST(stock AS DECIMAL(20,0))+#{delta} BETWEEN 0 AND"
                    + " 2147483647 AND CAST(stock AS DECIMAL(20,0))+#{delta}+(SELECT"
                    + " COALESCE(SUM(r.quantity),0) FROM mall_stock_reservation r WHERE"
                    + " r.product_id=#{productId} AND r.status='RESERVED') <= 2147483647")
    int adjustStock(StockAdjustmentDO row);

    @Update(
            "UPDATE mall_stock_adjustment SET resulting_stock=#{resultingStock} WHERE"
                    + " request_no=#{requestNo}")
    int finishAdjustment(StockAdjustmentDO row);
}
