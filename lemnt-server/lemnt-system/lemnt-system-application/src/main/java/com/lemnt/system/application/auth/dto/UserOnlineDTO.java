package com.lemnt.system.application.auth.dto;

import lombok.Data;

/**
 * 在线用户 DTO
 */
@Data
public class UserOnlineDTO {

    /**
     * 会话编号
     */
    private String tokenId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 登录IP地址
     */
    private String ipaddr;

    /**
     * 登录时间
     */
    private Long loginTime;
}
