package com.lemnt.system.application.auth.executor;

import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.application.auth.qry.MenuListQry;
import com.lemnt.system.domain.auth.gateway.MenuGateway;
import com.lemnt.system.domain.auth.model.Menu;
import com.lemnt.system.domain.auth.model.TreeSelect;
import com.lemnt.system.application.auth.service.TreeMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 菜单列表查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class MenuTreeSelectQueryExe {

    private final MenuGateway menuGateway;
    private final TreeMenuService treeMenuService;

    /**
     * 按条件查询菜单列表
     *
     * @param qry 查询条件
     * @return 菜单 DTO 列表
     */
    public List<TreeSelect> execute(MenuListQry qry) {
        Menu query = new Menu();
        query.setMenuName(qry.menuName());
        query.setStatus(qry.status());
        query.setVisible(qry.visible());
        List<Menu> list;
        if (SecurityUtils.isAdmin(qry.userId())) {
            list = menuGateway.selectList(query);
        } else {
            query.setUserId(qry.userId());
            list = menuGateway.selectListByUserId(query);
        }
        return treeMenuService.buildTreeSelect(list);
    }
}
