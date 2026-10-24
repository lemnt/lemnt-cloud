package com.lemnt.common.adapter.res;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 适配层响应对象基类
 *
 * @author wangxx
 */
public abstract class BaseRes implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
