package com.lemnt.trade.infrastructure.order.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** trade_order_log 数据对象，仅包含数据库字段。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trade_order_log")
public class OrderLogDO extends BaseDO {
    /** 日志编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 会员或后台用户编号，系统为0。 */
    private Long userId;
    /** 系统0、会员1、后台2。 */
    private Integer userType;
    /** 订单主键。 */
    private Long orderId;
    /** 操作前状态，新建时允许空。 */
    private Integer beforeStatus;
    /** 操作后状态。 */
    private Integer afterStatus;
    /** 芋道订单操作类型编号。 */
    private Integer operateType;
    /** 操作说明。 */
    private String content;
    /** 逻辑删除标记。 */
    @TableLogic(value = "0", delval = "1")
    private Boolean deleted;
}
