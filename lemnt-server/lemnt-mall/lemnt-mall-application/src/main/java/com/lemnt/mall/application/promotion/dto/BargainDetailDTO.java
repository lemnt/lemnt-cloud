package com.lemnt.mall.application.promotion.dto;

import java.util.List;

/** App 砍价详情：活动、记录、助力列表及本人是否已助力。 */
public record BargainDetailDTO(
        BargainActivityDTO activity,
        BargainRecordDTO record,
        List<BargainHelpDTO> helps,
        Boolean helped
) {}
