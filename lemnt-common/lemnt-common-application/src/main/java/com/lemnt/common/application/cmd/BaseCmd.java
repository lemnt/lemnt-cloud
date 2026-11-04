package com.lemnt.common.application.cmd;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 命令对象基类
 *
 * @author wangxx
 */
@Data
public abstract class BaseCmd implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
