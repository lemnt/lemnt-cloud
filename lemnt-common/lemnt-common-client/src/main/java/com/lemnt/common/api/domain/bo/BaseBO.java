package com.lemnt.common.api.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * BO 基类
 *
 * @author wangxx
 */
@Data
public class BaseBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
