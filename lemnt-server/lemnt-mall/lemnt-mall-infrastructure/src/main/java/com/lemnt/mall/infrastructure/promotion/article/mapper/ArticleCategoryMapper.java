package com.lemnt.mall.infrastructure.promotion.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lemnt.mall.infrastructure.promotion.article.dataobject.ArticleCategoryDO;
import org.apache.ibatis.annotations.Mapper;

/** 文章分类表Mapper，复用MyBatis Plus通用CRUD。 */
@Mapper
public interface ArticleCategoryMapper extends BaseMapper<ArticleCategoryDO> {
    /** 锁定分类，保护删除与文章写入的引用关系。 */
    ArticleCategoryDO lock(Long id);
}
