package com.lemnt.mall.domain.promotion.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.model.BargainHelp;

import java.util.List;

/** 砍价助力存储契约。 */
public interface BargainHelpGateway {
    BargainHelp save(BargainHelp value);

    BargainHelp byRecordAndUser(Long recordId, Long userId);

    List<BargainHelp> byRecordId(Long recordId);

    long countByUserAndActivity(Long userId, Long activityId);

    PageResult<BargainHelp> page(Long recordId, Long userId, Long activityId, int pageNum, int pageSize);
}
