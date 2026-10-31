package com.lemnt.trade.application.fulfillment.dto;

import java.util.List;

/**
 * 退款分页数据传输对象。
 */
public record RefundPageDTO(
        List<RefundRequestDTO> records,
        long total
) implements java.io.Serializable {}
