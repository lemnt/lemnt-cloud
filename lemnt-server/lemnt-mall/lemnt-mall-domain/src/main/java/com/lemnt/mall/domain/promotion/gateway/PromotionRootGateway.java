package com.lemnt.mall.domain.promotion.gateway;

import com.lemnt.mall.domain.promotion.model.PromotionDraft;
import com.lemnt.mall.domain.promotion.model.PromotionFilter;
import com.lemnt.mall.domain.promotion.model.PromotionReceipt;
import com.lemnt.mall.domain.promotion.model.PromotionRoot;

import java.util.List;

/** PromotionRoot 表领域存储接口。 */
public interface PromotionRootGateway {
    /**
     * 转换分页查询或分页响应。
     */
    List<PromotionReceipt> page(PromotionFilter filter);

    /**
     * 数量。
     */
    long count(PromotionFilter filter);

    /**
     * 当前时间毫秒时间戳。
     */
    long nowEpochMs();

    /**
     * 根节点。
     */
    PromotionRoot root(long id);

    /**
     * 锁定根节点。
     */
    PromotionRoot lockRoot(long id);

    /**
     * 保存根节点。
     */
    void saveRoot(PromotionRoot root, long expected, long operator);

    /** 计算持久化草稿摘要。 */
    String draftHash(PromotionDraft d);

    /** 保存促销主体，计数器由应用层创建。 */
    long insertRoot(String code, PromotionDraft draft, long o);
}
