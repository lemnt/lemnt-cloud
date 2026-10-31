package com.lemnt.system.adapter.web.auth.res;

import com.lemnt.system.application.auth.dto.MetaDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/**
 * 路由信息响应
 *
 * @author wangxx
 */
@Schema(description = "路由信息响应")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record RouterRes(
        /**
 * 路由名字
 */
        @Schema(description = "路由名字")
        String name,
        /**
 * 路由地址
 */
        @Schema(description = "路由地址")
        String path,
        /**
 * 是否隐藏路由，当设置 true 的时候该路由不会再侧边栏出现
 */
        @Schema(description = "是否隐藏路由")
        boolean hidden,
        /**
 * 重定向地址，当设置 noRedirect 的时候该路由在面包屑导航中不可被点击
 */
        @Schema(description = "重定向地址")
        String redirect,
        /**
 * 组件地址
 */
        @Schema(description = "组件地址")
        String component,
        /**
 * 路由参数：如 {"id": 1, "name": "ry"}
 */
        @Schema(description = "路由参数")
        String query,
        /**
 * 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
 */
        @Schema(description = "是否总是显示")
        Boolean alwaysShow,
        /**
 * 其他元素
 */
        @Schema(description = "其他元素")
        MetaDTO meta,
        /**
 * 子路由
 */
        @Schema(description = "子路由")
        List<RouterRes> children
) implements Serializable {}
