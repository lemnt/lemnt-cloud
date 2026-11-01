package com.lemnt.mall.application.promotion.cmd;

import java.io.Serializable;

/** 装修属性更新命令。 */
public record DiyPropertyCmd(Long id, String property) implements Serializable {
}
