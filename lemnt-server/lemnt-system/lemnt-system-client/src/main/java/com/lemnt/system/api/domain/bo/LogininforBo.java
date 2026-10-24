package com.lemnt.system.api.domain.bo;

import com.lemnt.common.api.domain.bo.BaseBO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 系统访问记录表 sys_logininfor
 *
 * @author wangxx
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class LogininforBo extends BaseBO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long infoId;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 登录类型 1 登录 2 退出
     */
    private Integer loginType;

    /**
     * 登录方式 1 账号密码
     */
    private Integer loginMethod;

    /**
     * 状态 1成功 2失败
     */
    private String status;

    /**
     * 地址
     */
    private String ipaddr;

    /**
     * 描述
     */
    private String msg;

    /**
     * 访问时间
     */
    private LocalDateTime accessTime;

    /**
     * 开始时间
     */
    private LocalDateTime beginTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}