package com.lemnt.mall.domain.settlement.gateway;

import com.lemnt.mall.domain.settlement.model.Settlement;
import com.lemnt.mall.domain.settlement.model.SettlementRecord;
import com.lemnt.mall.domain.settlement.model.SettlementSubmission;

import java.util.List;

/** 结算预览、提交上下文及索引的缓存边界，不保存交易订单。 */
public interface SettlementGateway {
    /** 缓存十五分钟结算预览。 */
    void cache(Settlement value);
    /** 读取结算预览。 */
    Settlement cached(String id);
    /** 原子缓存提交内容、请求索引和购物车引用索引。 */
    void saveSubmission(SettlementSubmission value);
    /** 按原订单号恢复提交上下文。 */
    SettlementSubmission submission(String orderNo);
    /** 读取原请求回执。 */
    SettlementRecord findRequest(Long memberId, String requestNo);
    /** 查询购物车条目被哪些未过期提交引用。 */
    List<String> ordersForCart(Long cartId);
    /** 查询结算单绑定的原订单号。 */
    String orderNo(Long memberId, String settlementId);
    /** 幂等绑定结算单与订单号。 */
    void bind(Long memberId, String settlementId, String orderNo);
    /** 查询结算单是否已放弃。 */
    boolean abandoned(Long memberId, String settlementId);
    /** 标记结算单已放弃，阻止迟到提交。 */
    void abandon(Long memberId, String settlementId);
}
