package com.lemnt.mall.application.promotion.article.service;

import com.lemnt.common.core.exception.ServiceException;

/** 文章边界校验，服务调用与HTTP保持相同规则。 */
final class ArticleValidation {
    private ArticleValidation() {}
    static void id(Long id) {
        if (id == null || id <= 0) {
            throw new ServiceException("编号不合法", 400);
        }
    }
    static void text(String value, int max, boolean required) {
        if ((required && (value == null || value.isBlank())) || (value != null && value.length() > max)) {
            throw new ServiceException("文本长度或内容不合法", 400);
        }
    }
    static void state(Integer status, Integer sort) {
        if (status == null || (status != 0 && status != 1) || sort == null || sort < 0) {
            throw new ServiceException("状态或排序不合法", 400);
        }
    }
    static void page(int pageNum, int pageSize, java.time.LocalDateTime from, java.time.LocalDateTime to) {
        if (pageNum < 1 || pageNum > 1000000 || pageSize < 1 || pageSize > 100
                || (from != null && to != null && from.isAfter(to))) {
            throw new ServiceException("分页或时间范围不合法", 400);
        }
    }
}
