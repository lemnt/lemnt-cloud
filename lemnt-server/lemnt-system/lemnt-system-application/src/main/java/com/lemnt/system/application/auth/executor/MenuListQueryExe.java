package com.lemnt.system.application.auth.executor;

import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.application.auth.converter.MenuConverter;
import com.lemnt.system.application.auth.dto.MenuDTO;
import com.lemnt.system.application.auth.qry.MenuListQry;
import com.lemnt.system.domain.auth.gateway.MenuGateway;
import com.lemnt.system.domain.auth.model.Menu;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 菜单列表查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class MenuListQueryExe {
    private final MenuGateway menuGateway;

    /**
     * 按条件查询菜单列表
     *
     * @param qry 查询条件
     * @return 菜单 DTO 列表
     */
    public List<MenuDTO> execute(MenuListQry qry) {
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
        return MenuConverter.toDTOList(list);
    }
}
