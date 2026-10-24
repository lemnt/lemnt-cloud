package com.lemnt.common.application.qry;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 查询对象基类
 *
 * @author wangxx
 */
@Data
public abstract class BaseQry implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
