package com.lemnt.mall.infrastructure.promotion.article.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import com.lemnt.common.infrastructure.domain.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/** mall_promotion_article_category，按表独立存储对象。 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("mall_promotion_article_category")
public class ArticleCategoryDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String picUrl;
    private Integer status;
    private Integer sort;
    @TableLogic
    private Boolean deleted;
}
