package com.lemnt.promotion.infrastructure.article.mapper;

import com.lemnt.promotion.infrastructure.article.dataobject.ArticleDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleMapper extends BaseMapper<ArticleDO> {}
