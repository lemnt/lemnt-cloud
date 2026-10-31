package com.lemnt.trade.infrastructure.payment.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** trade_pay_notify_log 数据对象。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trade_pay_notify_log")
public class PayNotifyLogDO extends BaseDO {
    /** 日志编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 任务编号。 */
    private Long taskId;

    /** 第几次尝试。 */
    private Integer notifyTimes;

    /** 结果：SUCCESS/FAILED。 */
    private String status;

    /** 响应或失败信息。 */
    private String response;
}
