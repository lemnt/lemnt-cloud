package com.lemnt.product.application.product.cmd;

import java.io.Serializable;

/** 创建规格项。 */
public record ProductSpecCreateCmd(String name, String remark) implements Serializable {}
