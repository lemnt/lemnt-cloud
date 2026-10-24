package com.lemnt.system.application.auth.cmd;

import java.io.Serializable;

/**
 * 修改菜单命令
 *
 * @author wangxx
 */
public record MenuUpdateCmd(
        /**
         * 菜单ID
         */
        Long menuId,
        /**
         * 菜单名称
         */
        String menuName,
        /**
         * 父菜单ID
         */
        Long parentId,
        /**
         * 显示顺序
         */
        Integer orderNum,
        /**
         * 路由地址
         */
        String path,
        /**
         * 组件路径
         */
        String component,
        /**
         * 路由参数
         */
        String query,
        /**
         * 路由名称
         */
        String routeName,
        /**
         * 是否为外链（0是 1否）
         */
        String isFrame,
        /**
         * 是否缓存（0缓存 1不缓存）
         */
        String isCache,
        /**
         * 菜单类型（M目录 C菜单 F按钮）
         */
        String menuType,
        /**
         * 显示状态（0显示 1隐藏）
         */
        String visible,
        /**
         * 菜单状态（1正常 2停用）
         */
        String status,
        /**
         * 权限标识
         */
        String perms,
        /**
         * 菜单图标
         */
        String icon
) implements Serializable {
}
