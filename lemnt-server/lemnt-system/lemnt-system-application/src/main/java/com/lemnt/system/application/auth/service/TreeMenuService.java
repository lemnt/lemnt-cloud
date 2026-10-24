package com.lemnt.system.application.auth.service;

import com.lemnt.system.domain.auth.model.Menu;
import com.lemnt.system.domain.auth.model.TreeSelect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TreeMenuService {

    /**
     * 构建前端所需要下拉树结构
     *
     * @param menuList 菜单列表
     * @return 下拉树结构列表
     */
    public List<TreeSelect> buildTreeSelect(List<Menu> menuList) {
        List<Menu> menuTrees = buildMenuTree(menuList);
        return menuTrees.stream().map(TreeSelect::new).collect(Collectors.toList());
    }

    /**
     * 构建前端所需要树结构
     *
     * @param menus 菜单列表
     * @return 树结构列表
     */
    public List<Menu> buildMenuTree(List<Menu> menus) {
        List<Menu> returnList = new ArrayList<>();
        List<Long> tempList = menus.stream().map(Menu::getMenuId).toList();
        for (Menu menu : menus) {
            // 如果是顶级节点, 遍历该父节点的所有子节点
            if (!tempList.contains(menu.getParentId())) {
                recursionFn(menus, menu);
                returnList.add(menu);
            }
        }
        if (returnList.isEmpty()) {
            returnList = menus;
        }
        return returnList;
    }


    /**
     * 递归列表
     */
    private void recursionFn(List<Menu> list, Menu t) {
        // 得到子节点列表
        List<Menu> childList = getChildList(list, t);
        t.setChildren(childList);
        for (Menu tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<Menu> getChildList(List<Menu> list, Menu t) {
        List<Menu> tlist = new ArrayList<>();
        for (Menu n : list) {
            if (n.getParentId().longValue() == t.getMenuId().longValue()) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<Menu> list, Menu t) {
        return !getChildList(list, t).isEmpty();
    }
}
