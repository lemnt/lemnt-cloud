package com.lemnt.common.core.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额工具类
 *
 * @author wangxx
 */
public class AmountUtil {

    // 元 → 分（BigDecimal 转 long，避免精度丢失）
    public static long yuanToCent(BigDecimal yuan) {
        return yuan.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).longValue();
    }

    // 分 → 元（Long 转 BigDecimal，null 返回 null）
    public static BigDecimal centToYuan(Long cent) {
        if (cent == null) return null;
        return new BigDecimal(cent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    // 分 → 百分比（long 转 BigDecimal）
    public static BigDecimal centToPercent(BigDecimal costPrice, BigDecimal total) {
        // 1. 先获取核心数值，做空值/零值兜底
        BigDecimal cost = costPrice == null ? BigDecimal.ZERO : costPrice;
        BigDecimal budget = total == null ? BigDecimal.ZERO : total;

        BigDecimal spendingPercentage;
        if (BigDecimal.ZERO.equals(budget) || BigDecimal.ZERO.equals(cost)) {
            // 预算/成本为0时，百分比为0
            spendingPercentage = BigDecimal.ZERO;
        } else {
            // 2. 核心修正：除以预算后乘以100，再保留2位小数（四舍五入）
            spendingPercentage = cost.divide(budget, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return spendingPercentage;
    }

    /**
     * 整数转两位小数（n → n/100，默认四舍五入）
     *
     * @param num 待转换整数（可为 null，默认返回 0.00）
     * @return 两位小数的 BigDecimal
     */
    public static BigDecimal intToTwoDecimal(Integer num) {
        // 处理 null：null 视为 0
        int input = num == null ? 0 : num;
        return new BigDecimal(input)
                .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
    }
}
