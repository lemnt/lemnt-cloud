package com.lemnt.promotion.client.dto;

import java.io.Serializable;
/** 原购物车行版本，提交不能误删会员后续修改。 */
public record CartSnapshotDTO(Long id, Long memberId, Long productId, int quantity, boolean selected, long version) implements Serializable {  }
