package com.lemnt.promotion.domain.promotion.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.promotion.domain.promotion.model.DiyTemplate;
import com.lemnt.promotion.domain.promotion.model.DiyTemplateFilter;

/** 装修模板存储契约。 */
public interface DiyTemplateGateway {
    DiyTemplate find(Long id);

    DiyTemplate findByName(String name);

    DiyTemplate findUsed();

    DiyTemplate save(DiyTemplate value);

    PageResult<DiyTemplate> page(DiyTemplateFilter filter);

    void delete(Long id);
}
