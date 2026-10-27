package com.lemnt.system.adapter.web.auth.converter;

import com.lemnt.system.adapter.web.auth.req.MenuAddReq;
import com.lemnt.system.adapter.web.auth.req.MenuListReq;
import com.lemnt.system.adapter.web.auth.req.MenuUpdateReq;
import com.lemnt.system.adapter.web.auth.req.SystemMenuListReq;
import com.lemnt.system.adapter.web.auth.req.SystemMenuTreeSelectReq;
import com.lemnt.system.application.auth.cmd.MenuAddCmd;
import com.lemnt.system.application.auth.cmd.MenuUpdateCmd;
import com.lemnt.system.application.auth.qry.MenuListQry;

/**
 * 菜单请求对象转换器
 *
 * @author wangxx
 */
public class MenuReqConverter {
    public static MenuAddCmd toCmd(MenuAddReq req) {
        return new MenuAddCmd(
                req.menuName(),
                req.parentId(),
                req.orderNum(),
                req.path(),
                req.component(),
                req.query(),
                req.routeName(),
                req.isFrame(),
                req.isCache(),
                req.menuType(),
                req.visible(),
                req.status(),
                req.perms(),
                req.icon()
        );
    }

    public static MenuUpdateCmd toCmd(MenuUpdateReq req) {
        return new MenuUpdateCmd(
                req.menuId(),
                req.menuName(),
                req.parentId(),
                req.orderNum(),
                req.path(),
                req.component(),
                req.query(),
                req.routeName(),
                req.isFrame(),
                req.isCache(),
                req.menuType(),
                req.visible(),
                req.status(),
                req.perms(),
                req.icon()
        );
    }

    public static MenuListQry toQry(MenuListReq req, Long userId) {
        if (req == null) {
            return null;
        }
        return new MenuListQry(
                req.menuName(),
                req.status(),
                req.visible(),
                userId
        );
    }

    public static MenuListQry toQry(SystemMenuListReq req, Long userId) {
        if (req == null) {
            return null;
        }
        return new MenuListQry(
                req.menuName(),
                req.status(),
                req.visible(),
                userId
        );
    }

    public static MenuListQry toQry(SystemMenuTreeSelectReq req, Long userId) {
        if (req == null) {
            return null;
        }
        return new MenuListQry(
                req.menuName(),
                req.status(),
                req.visible(),
                userId
        );
    }
}
