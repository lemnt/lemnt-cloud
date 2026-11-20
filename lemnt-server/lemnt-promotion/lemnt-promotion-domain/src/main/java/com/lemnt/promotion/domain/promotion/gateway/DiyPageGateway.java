package com.lemnt.promotion.domain.promotion.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.promotion.domain.promotion.model.DiyPage;
import com.lemnt.promotion.domain.promotion.model.DiyPageFilter;

import java.util.List;

/**
 * 装修页面存储契约。
 */
public interface DiyPageGateway {
    DiyPage find(Long id);

    DiyPage findByName(Long templateId, String name);

    List<DiyPage> byTemplateId(Long templateId);

    DiyPage save(DiyPage value);

    PageResult<DiyPage> page(DiyPageFilter filter);

    void delete(Long id);
}
