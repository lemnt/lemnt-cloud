package com.lemnt.mall.infrastructure.promotion.article.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import com.lemnt.common.infrastructure.domain.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/** mall_promotion_article，按表独立存储对象。 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("mall_promotion_article")
public class ArticleDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long categoryId;
    private Long spuId;
    private String title;
    private String author;
    private String picUrl;
    private String introduction;
    private Integer sort;
    private Integer status;
    private Boolean recommendHot;
    private Boolean recommendBanner;
    private String content;
    private Integer browseCount;
    @TableLogic
    private Boolean deleted;
}
