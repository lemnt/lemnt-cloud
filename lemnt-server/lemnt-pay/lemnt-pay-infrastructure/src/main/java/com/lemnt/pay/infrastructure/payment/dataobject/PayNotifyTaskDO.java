package com.lemnt.pay.infrastructure.payment.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** pay_notify_task 数据对象。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_notify_task")
public class PayNotifyTaskDO extends BaseDO {
    /** 任务编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 类型：PAY/REFUND/AFTER_SALE。 */
    private String type;

    /** 业务单号：支付单号或退款单号。 */
    private String bizNo;

    /** 订单编号。 */
    private String orderNo;

    /** 状态：PENDING/SUCCESS/FAILED。 */
    private String status;

    /** 已尝试次数。 */
    private Integer notifyTimes;

    /** 最大尝试次数。 */
    private Integer maxNotifyTimes;

    /** 下次派发时间。 */
    private LocalDateTime nextNotifyTime;

    /** 最后一次执行时间。 */
    private LocalDateTime lastExecuteTime;

    /** 最后一次响应或失败信息。 */
    private String lastResponse;

    /** 每次领取和重置递增，旧执行者不得覆盖新状态。 */
    private Long executeVersion;

    /** 领取租约到期后可恢复执行。 */
    private LocalDateTime leaseUntil;
}
