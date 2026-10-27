package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.mall.domain.promotion.gateway.PromotionRootGateway;
import com.lemnt.mall.domain.promotion.model.PromotionDraft;
import com.lemnt.mall.domain.promotion.model.PromotionFilter;
import com.lemnt.mall.domain.promotion.model.PromotionReceipt;
import com.lemnt.mall.domain.promotion.model.PromotionRoot;
import com.lemnt.mall.infrastructure.promotion.converter.PromotionConverter;
import com.lemnt.mall.infrastructure.promotion.dataobject.PromotionRootDO;
import com.lemnt.mall.infrastructure.promotion.mapper.PromotionRootMapper;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** PromotionRoot 表持久化实现。 */
@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class PromotionRootGatewayImpl implements PromotionRootGateway {
    private final PromotionRootMapper promotionRootMapper;

    @Override
    /**
     * 转换分页查询或分页响应。
     */
    public List<PromotionReceipt> page(PromotionFilter filter) {
        return promotionRootMapper.page(filter).stream()
                .map(
                r ->
                new PromotionReceipt(
                r.getId(),
                r.getRevision(),
                PromotionRoot.State.valueOf(r.getState()),
                r.getActiveVersion(),
                r.getLastVersion()))
                .toList();
    }

    @Override
    /**
     * 数量。
     */
    public long count(PromotionFilter filter) {
        return promotionRootMapper.count(filter);
    }

    @Override
    /**
     * 当前时间毫秒时间戳。
     */
    public long nowEpochMs() {
        return promotionRootMapper.nowEpochMs();
    }

    @Override
    /**
     * 根节点。
     */
    public PromotionRoot root(long id) {
        return PromotionConverter.root(promotionRootMapper.root(id));
    }

    @Override
    /**
     * 锁定根节点。
     */
    public PromotionRoot lockRoot(long id) {
        return PromotionConverter.root(promotionRootMapper.lockRoot(id));
    }

    @Override
    /**
     * 保存根节点。
     */
    public void saveRoot(PromotionRoot r, long e, long o) {
        one(promotionRootMapper.saveRoot(PromotionConverter.row(r), e, o));
    }

    private static void one(int n) {
        if (n != 1) {
            throw new IllegalStateException("Promotion persistence CAS/integrity failure");
        }
    }

    /** 计算持久化草稿摘要。 */
    @Override
    /**
     * 草稿摘要。
     */
    public String draftHash(PromotionDraft d) {
        return PromotionConverter.hash(d);
    }

    /** 保存促销主体，计数器由应用层创建。 */
    @Override
    /**
     * 创建根节点。
     */
    public long insertRoot(String code, PromotionDraft draft, long o) {
        var r = new PromotionRootDO();
        r.setCode(code);
        r.setDraftJson(PromotionConverter.encode(draft));
        one(promotionRootMapper.insertRoot(r, o));
        return r.getId();
    }
}
