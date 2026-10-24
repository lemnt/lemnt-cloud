package com.lemnt.mall.infrastructure.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;import com.lemnt.mall.infrastructure.dataobject.ProductDO;import org.apache.ibatis.annotations.Mapper;import org.apache.ibatis.annotations.Param;import org.apache.ibatis.annotations.Update;
@Mapper public interface ProductMapper extends BaseMapper<ProductDO> {
 @Update("UPDATE mall_product SET stock=stock-#{quantity}, update_time=CURRENT_TIMESTAMP WHERE id=#{id} AND status='ON_SHELF' AND stock>=#{quantity}") int reserveStock(@Param("id") Long id,@Param("quantity") Integer quantity);
 @Update("UPDATE mall_product SET stock=stock+#{quantity}, update_time=CURRENT_TIMESTAMP WHERE id=#{id}") int releaseStock(@Param("id") Long id,@Param("quantity") Integer quantity);
}