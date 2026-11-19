package com.lemnt.product.infrastructure.product.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品SPU数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("product_spu")
public class ProductSpuDO implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;
    private String name;
    private String keyword;
    private String introduction;
    private String sliderPicUrls;
    private Integer sort;
    private Long categoryId;
    private Long brandId;
    private String description;
    private String image;
    private String status;
    private String deliveryTypes;
    private Long deliveryTemplateId;

    /** 销量：支付确认事实累计。 */
    private Integer salesCount;

    private Integer virtualSalesCount;
    private Boolean specType;
    private Integer giveIntegral;
    private Boolean subCommissionType;
    private Long browseCount;

    /** SPU真实创建时间，历史未知保留空值。 */
    private LocalDateTime createTime;
}
