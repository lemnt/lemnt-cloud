package com.lemnt.product.application.product.cmd;

import java.io.Serializable;

/** 修改规格项。 */
public record ProductSpecUpdateCmd(Long id, String name, String remark) implements Serializable {}
