package com.lemnt.system.application.auth.qry;

import java.io.Serializable;

/**
 * 菜单列表查询条件
 *
 * @author wangxx
 */
public record MenuListQry(
        /**
         * 菜单名称
         */
        String menuName,
        /**
         * 菜单状态（1正常 2停用）
         */
        String status,
        /**
         * 显示状态（0显示 1隐藏）
         */
        String visible,
        /**
         * 查询用户ID（非管理员时按用户权限过滤）
         */
        Long userId
) implements Serializable {
}
