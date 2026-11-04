package com.lemnt.order.application.pickup.cmd;

import java.io.Serializable;

/**
 * 按核销码确认自提命令。
 */
public record PickUpVerifyCmd(String verifyCode, Long operatorId) implements Serializable {
}
