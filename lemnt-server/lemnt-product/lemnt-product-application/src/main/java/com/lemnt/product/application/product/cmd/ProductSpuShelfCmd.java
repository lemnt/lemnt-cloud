package com.lemnt.product.application.product.cmd;

import java.io.Serializable;

/**
 * 商品商品SPU上架命令。
 */
public record ProductSpuShelfCmd(Long id, boolean onShelf) implements Serializable {}
