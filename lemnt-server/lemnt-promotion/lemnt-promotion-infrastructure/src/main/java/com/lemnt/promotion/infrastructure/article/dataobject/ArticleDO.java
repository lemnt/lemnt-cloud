package com.lemnt.promotion.infrastructure.article.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("promotion_article")
public class ArticleDO {
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
    private Long browseCount;
}
