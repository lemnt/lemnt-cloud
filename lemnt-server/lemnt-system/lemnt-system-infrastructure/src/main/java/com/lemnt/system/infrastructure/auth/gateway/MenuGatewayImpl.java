package com.lemnt.system.infrastructure.auth.gateway;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.system.domain.auth.model.Menu;
import com.lemnt.system.domain.auth.gateway.MenuGateway;
import com.lemnt.system.infrastructure.auth.converter.MenuConverter;
import com.lemnt.system.infrastructure.auth.mapper.MenuMapper;

/**
 * 菜单网关实现 — infrastructure 层适配器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class MenuGatewayImpl implements MenuGateway {

    private final MenuMapper menuMapper;

    @Override
    public Menu selectById(Long menuId) {
        return MenuConverter.toEntity(menuMapper.selectById(menuId));
    }

    @Override
    public List<Menu> selectList(Menu query) {
        return MenuConverter.toEntityList(menuMapper.selectMenuList(query));
    }

    @Override
    public List<Menu> selectListByUserId(Menu query) {
        return MenuConverter.toEntityList(menuMapper.selectMenuListByUserId(query));
    }

    @Override
    public List<Menu> selectMenuTreeAll() {
        return MenuConverter.toEntityList(menuMapper.selectMenuTreeAll());
    }

    @Override
    public List<Menu> selectMenuTreeByUserId(Long userId) {
        return MenuConverter.toEntityList(menuMapper.selectMenuTreeByUserId(userId));
    }

    @Override
    public List<Long> selectMenuListByRoleId(Long roleId) {
        return menuMapper.selectMenuListByRoleId(roleId);
    }

    @Override
    public List<String> selectMenuPerms() {
        return menuMapper.selectMenuPerms();
    }

    @Override
    public List<String> selectMenuPermsByUserId(Long userId) {
        return menuMapper.selectMenuPermsByUserId(userId);
    }

    @Override
    public List<String> selectMenuPermsByRoleId(Long roleId) {
        return menuMapper.selectMenuPermsByRoleId(roleId);
    }

    @Override
    public Boolean insert(Menu entity) {
        return menuMapper.insert(MenuConverter.toDO(entity)) > 0;
    }

    @Override
    public Boolean update(Menu entity) {
        return menuMapper.updateById(MenuConverter.toDO(entity)) > 0;
    }

    @Override
    public Boolean deleteById(Long menuId) {
        return menuMapper.deleteById(menuId) > 0;
    }

    @Override
    public Boolean hasChildByMenuId(Long menuId) {
        return menuMapper.hasChildByMenuId(menuId) > 0;
    }

    @Override
    public Menu checkMenuNameUnique(String menuName, Long parentId) {
        return checkMenuNameUnique(menuName, parentId, null);
    }

    @Override
    public Menu checkMenuNameUnique(String menuName, Long parentId, Long excludeMenuId) {
        return MenuConverter.toEntity(menuMapper.checkMenuNameUnique(menuName, parentId, excludeMenuId));
    }
}
