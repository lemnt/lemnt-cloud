package com.lemnt.order.infrastructure.brokerage.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 提现单持久化对象。
 */
@Data
@TableName("order_brokerage_withdraw")
public class BrokerageWithdrawDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private BigDecimal price;
    private BigDecimal feePrice;
    private BigDecimal totalPrice;
    private Integer type;
    private String userName;
    private String userAccount;
    private String qrCodeUrl;
    private String bankName;
    private String bankAddress;
    private Integer status;
    private String auditReason;
    private LocalDateTime auditTime;
    private String remark;
    private LocalDateTime createTime;
}
