package com.lemnt.promotion.client.bo;

import com.lemnt.promotion.client.dto.PromotionItemDTO;

import java.io.Serializable;
import java.util.List;

/**
 * 新报价输入，金额只能来自可信商品快照；会员折扣和积分由服务端核实。
 */
public record PromotionCalculateBo(
    Long memberId,
    String requestNo,
    List<PromotionItemDTO> items,
    Long couponId,
    boolean pointStatus)
    implements Serializable {
}
