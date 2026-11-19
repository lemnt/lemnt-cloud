package com.lemnt.promotion.infrastructure.article.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("promotion_article_category")
public class ArticleCategoryDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String picUrl;
    private Integer sort;
    private Integer status;
}
