package com.lemnt.order.application.pickup.cmd;

import java.io.Serializable;
import java.util.List;

/**
 * 绑定门店核销店员命令。
 */
public record PickUpBindCmd(Long storeId, List<Long> verifyUserIds) implements Serializable {
}
