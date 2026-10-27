package com.lemnt.mall.client.bo;

import java.io.Serializable;
import java.util.List;

/**
 * 计价事实批量远程参数。
 */
public record PricingFactsBatchBo(
        List<Long> skuIds
) implements Serializable {}
