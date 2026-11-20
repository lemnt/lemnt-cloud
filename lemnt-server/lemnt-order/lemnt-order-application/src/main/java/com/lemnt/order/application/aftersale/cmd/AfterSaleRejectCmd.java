package com.lemnt.order.application.aftersale.cmd;

import java.io.Serializable;

/**
 * 拒绝商品售后。
 */
public record AfterSaleRejectCmd(String afterSaleNo, String reason) implements Serializable {
}
