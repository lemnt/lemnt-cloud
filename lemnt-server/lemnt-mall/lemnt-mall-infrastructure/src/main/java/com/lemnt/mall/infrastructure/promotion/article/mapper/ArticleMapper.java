package com.lemnt.mall.infrastructure.promotion.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lemnt.mall.infrastructure.promotion.article.dataobject.ArticleDO;
import org.apache.ibatis.annotations.Mapper;

/** 商城文章表Mapper，复用MyBatis Plus通用CRUD。 */
@Mapper
public interface ArticleMapper extends BaseMapper<ArticleDO> {

}
