package com.lemnt.common.api.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * dto 基类
 *
 * @author wangxx
 */
@Data
public class BaseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
