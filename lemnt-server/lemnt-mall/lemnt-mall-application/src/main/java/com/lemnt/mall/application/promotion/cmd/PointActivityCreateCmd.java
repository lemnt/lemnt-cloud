package com.lemnt.mall.application.promotion.cmd;

import java.util.List;

/** 新增积分商城活动命令。 */
public record PointActivityCreateCmd(Long spuId, String remark, Integer sort, List<PointProductCmd> products) {}
