package com.lemnt.system.adapter.web.notice.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/**
 * 首页顶部公告列表响应
 *
 * @author wangxx
 */
@Schema(description = "首页顶部公告列表响应")
public record NoticeTopRes(
        @Schema(description = "公告列表")
        List<NoticeRes> list,
        @Schema(description = "未读数量")
        Integer unreadCount
) implements Serializable {}
