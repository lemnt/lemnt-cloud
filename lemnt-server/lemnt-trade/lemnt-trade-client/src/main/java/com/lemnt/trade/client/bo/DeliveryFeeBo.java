package com.lemnt.trade.client.bo;

import java.io.Serializable;
import java.util.List;

/** 普通快递计价RPC输入。 */
public record DeliveryFeeBo(
        Integer areaId,
        Boolean freeDelivery,
        List<DeliveryFeeItemBo> items
) implements Serializable {
}
