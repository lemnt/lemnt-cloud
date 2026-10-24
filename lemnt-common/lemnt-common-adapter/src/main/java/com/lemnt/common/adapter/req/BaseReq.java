package com.lemnt.common.adapter.req;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 适配层请求对象基类
 *
 * @author wangxx
 */
@Data
public abstract class BaseReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
