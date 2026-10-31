package com.lemnt.mall.application.promotion.dto;

import java.util.List;

/** App 当前秒杀时段及活动。 */
public record SeckillNowDTO(SeckillConfigDTO config, List<SeckillAppActivityDTO> activities) {}
