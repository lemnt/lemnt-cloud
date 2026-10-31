package com.lemnt.mall.application.promotion.dto;

import java.util.List;

/** 拼团详情：团长记录、成员记录及当前用户订单。 */
public record CombinationRecordDetailDTO(
        CombinationRecordDTO headRecord,
        List<CombinationRecordDTO> memberRecords,
        Long orderId
) {}
