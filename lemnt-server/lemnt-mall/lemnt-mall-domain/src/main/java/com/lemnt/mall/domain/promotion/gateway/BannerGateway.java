package com.lemnt.mall.domain.promotion.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.model.Banner;
import com.lemnt.mall.domain.promotion.model.BannerFilter;

import java.util.List;

/** Banner 存储契约。 */
public interface BannerGateway {
    Banner find(Long id);

    Banner save(Banner value);

    void delete(Long id);

    PageResult<Banner> page(BannerFilter filter);

    /** App 列表只返回开启状态，并按排序值倒序。 */
    List<Banner> enabledByPosition(Integer position);

    /** 点击量原子加一。 */
    boolean increaseBrowseCount(Long id);
}
