package com.lemnt.order.client.event;

import java.io.Serializable;

/**
 * 订单服务校验后产生的冻结业务事实，不包含当前配置及个人敏感资料。
 */
public record OrderShippedEvent(String orderNo, Long memberId) implements Serializable {
}
