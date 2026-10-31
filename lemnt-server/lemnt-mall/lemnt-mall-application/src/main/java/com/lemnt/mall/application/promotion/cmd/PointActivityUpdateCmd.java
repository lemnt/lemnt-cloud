package com.lemnt.mall.application.promotion.cmd;

import java.util.List;

/** 修改积分商城活动命令。 */
public record PointActivityUpdateCmd(Long id, Long spuId, String remark, Integer sort,
        List<PointProductCmd> products) {}
