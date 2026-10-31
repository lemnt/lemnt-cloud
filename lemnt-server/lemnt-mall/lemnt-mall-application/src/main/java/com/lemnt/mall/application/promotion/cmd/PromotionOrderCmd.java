package com.lemnt.mall.application.promotion.cmd;

import java.util.List;

/** 活动订单统一下单命令，lines 为空时使用单商品字段。 */
public record PromotionOrderCmd(
        Long memberId,
        String activityType,
        Long activityId,
        Long recordId,
        Long headId,
        Long skuId,
        Integer quantity,
        Long addressId,
        String requestNo,
        List<PromotionOrderLineCmd> lines
) {
    /** 兼容单商品活动下单。 */
    public PromotionOrderCmd(Long memberId, String activityType, Long activityId, Long recordId, Long headId,
            Long skuId, Integer quantity, Long addressId, String requestNo) {
        this(memberId, activityType, activityId, recordId, headId, skuId, quantity, addressId, requestNo,
                skuId == null ? List.of() : List.of(new PromotionOrderLineCmd(skuId, quantity)));
    }

    public PromotionOrderCmd {
        lines = lines == null ? List.of() : List.copyOf(lines);
    }

    /** 多商品下单使用商品行集合，单字段保留首行和数量合计供兼容读取。 */
    public static PromotionOrderCmd batch(Long memberId, String activityType, Long activityId, Long recordId,
            Long headId, List<PromotionOrderLineCmd> lines, Long addressId, String requestNo) {
        var first = lines == null || lines.isEmpty() ? null : lines.get(0);
        var total = lines == null ? null : lines.stream().mapToInt(PromotionOrderLineCmd::quantity).sum();
        return new PromotionOrderCmd(memberId, activityType, activityId, recordId, headId,
                first == null ? null : first.skuId(), total, addressId, requestNo, lines);
    }
}
