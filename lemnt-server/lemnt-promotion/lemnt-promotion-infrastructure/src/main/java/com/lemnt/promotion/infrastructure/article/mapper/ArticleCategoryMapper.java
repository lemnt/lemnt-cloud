package com.lemnt.promotion.infrastructure.article.mapper;

import com.lemnt.promotion.infrastructure.article.dataobject.ArticleCategoryDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleCategoryMapper extends BaseMapper<ArticleCategoryDO> {}
