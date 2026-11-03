package com.lemnt.product.infrastructure.product.mapper;

import com.lemnt.product.infrastructure.product.dataobject.ProductCommentDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 商品评价表访问接口。 */
@Mapper
public interface ProductCommentMapper extends BaseMapper<ProductCommentDO> {
    /** 序列化商家回复，重复回复保留原内容与时间。 */
    ProductCommentDO lock(Long id);

    /** 旧购买唯一事实检查包含逻辑删除行，防止重复投稿或猜测旧订单项。 */
    java.util.List<com.lemnt.product.infrastructure.product.dataobject.ProductCommentDO>
            legacyForLink(@Param("orderNo") String orderNo, @Param("skuId") Long skuId);
}
