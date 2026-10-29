package com.lemnt.mall.infrastructure.product.mapper;

import com.lemnt.mall.infrastructure.product.dataobject.ProductCommentDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 商品评价表访问接口。 */
@Mapper
public interface ProductCommentMapper extends BaseMapper<ProductCommentDO> {
    /** 序列化商家回复，重复回复保留原内容与时间。 */
    ProductCommentDO lock(Long id);
    /** 旧购买唯一事实检查包含逻辑删除行，防止重复投稿或猜测旧订单项。 */
    java.util.List<com.lemnt.mall.infrastructure.product.dataobject.ProductCommentDO> legacyForLink(
            @org.apache.ibatis.annotations.Param("orderNo") String orderNo,
            @org.apache.ibatis.annotations.Param("skuId") Long skuId);
}
