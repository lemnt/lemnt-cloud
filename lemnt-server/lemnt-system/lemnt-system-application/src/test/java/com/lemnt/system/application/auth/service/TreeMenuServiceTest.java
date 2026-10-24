package com.lemnt.system.application.auth.service;

import com.lemnt.system.domain.auth.model.Menu;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TreeMenuServiceTest {

    private final TreeMenuService treeMenuService = new TreeMenuService();

    @Test
    void shouldAddEachChildOnlyOnce() {
        Menu parent = new Menu();
        parent.setMenuId(1L);
        parent.setParentId(0L);

        Menu child = new Menu();
        child.setMenuId(2L);
        child.setParentId(1L);

        List<Menu> tree = treeMenuService.buildMenuTree(List.of(parent, child));

        assertEquals(1, tree.size());
        assertEquals(1, tree.get(0).getChildren().size());
        assertEquals(2L, tree.get(0).getChildren().get(0).getMenuId());
    }
}
