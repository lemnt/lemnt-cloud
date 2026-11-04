package com.lemnt.common.adapter.req;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 适配层请求对象基类
 *
 * @author wangxx
 */
@Data
@Schema(description = "HTTP请求基类")
public abstract class BaseReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
