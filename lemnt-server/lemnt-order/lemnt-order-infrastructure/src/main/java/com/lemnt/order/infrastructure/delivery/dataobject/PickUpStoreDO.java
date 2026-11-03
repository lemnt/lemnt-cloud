package com.lemnt.order.infrastructure.delivery.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.math.BigDecimal;

/** 自提门店持久化对象。 */
@Data
@TableName("order_delivery_pick_up_store")
public class PickUpStoreDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;
    private String introduction;
    private String phone;
    private Long areaId;
    private String detailAddress;
    private String logo;
    private String openingTime;
    private String closingTime;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String verifyUserIds;
    private Integer status;
}
