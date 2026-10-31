package com.lemnt.system.domain.auth.gateway;

import com.lemnt.system.domain.auth.model.Menu;

import java.util.List;

/**
 * 菜单网关 — domain 层端口
 *
 * @author wangxx
 */
public interface MenuGateway {
    /**
     * 根据菜单ID查询信息
     *
     * @param menuId 菜单ID
     * @return 菜单信息
     */
    Menu selectById(Long menuId);

    /**
     * 查询菜单列表
     *
     * @param query 查询条件
     * @return 菜单集合
     */
    List<Menu> selectList(Menu query);

    /**
     * 根据用户查询菜单列表
     *
     * @param query 查询条件
     * @return 菜单集合
     */
    List<Menu> selectListByUserId(Menu query);

    /**
     * 查询所有菜单树
     *
     * @return 菜单列表
     */
    List<Menu> selectMenuTreeAll();

    /**
     * 根据用户ID查询菜单树
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<Menu> selectMenuTreeByUserId(Long userId);

    /**
     * 根据角色ID查询菜单ID列表
     *
     * @param roleId 角色ID
     * @return 选中菜单ID列表
     */
    List<Long> selectMenuListByRoleId(Long roleId);

    /**
     * 查询所有菜单权限
     *
     * @return 权限列表
     */
    List<String> selectMenuPerms();

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<String> selectMenuPermsByUserId(Long userId);

    /**
     * 根据角色ID查询权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<String> selectMenuPermsByRoleId(Long roleId);

    /**
     * 新增菜单
     *
     * @param entity 菜单信息
     * @return 是否成功
     */
    Boolean insert(Menu entity);

    /**
     * 修改菜单
     *
     * @param entity 菜单信息
     * @return 是否成功
     */
    Boolean update(Menu entity);

    /**
     * 删除菜单
     *
     * @param menuId 菜单ID
     * @return 是否成功
     */
    Boolean deleteById(Long menuId);

    /**
     * 是否存在子菜单
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    Boolean hasChildByMenuId(Long menuId);

    /**
     * 校验菜单名称是否唯一
     *
     * @param menuName 菜单名称
     * @param parentId 父菜单ID
     * @return 菜单信息
     */
    Menu checkMenuNameUnique(String menuName, Long parentId);

    /**
     * 查询同级名称冲突，修改时排除当前菜单。
     *
     * @param menuName 菜单名称
     * @param parentId 目标父菜单ID
     * @param excludeMenuId 排除的菜单ID，新增时为空
     * @return 冲突菜单信息
     */
    Menu checkMenuNameUnique(String menuName, Long parentId, Long excludeMenuId);
}
