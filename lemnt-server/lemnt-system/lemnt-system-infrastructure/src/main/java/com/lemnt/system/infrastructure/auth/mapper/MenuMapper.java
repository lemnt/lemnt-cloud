package com.lemnt.system.infrastructure.auth.mapper;

import com.lemnt.system.domain.auth.model.Menu;
import com.lemnt.system.infrastructure.auth.dataobject.MenuDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * Menu 数据层
 *
 * @author wangxx
 */
@Mapper
public interface MenuMapper extends BaseMapper<MenuDO> {
    /**
     * 查询系统菜单列表
     *
     * @param menu 菜单信息
     * @return 菜单列表
     */
    List<MenuDO> selectMenuList(Menu menu);

    /**
     * 根据用户所有权限
     *
     * @return 权限列表
     */
    List<String> selectMenuPerms();

    /**
     * 根据用户查询系统菜单列表
     *
     * @param menu 菜单信息
     * @return 菜单列表
     */
    List<MenuDO> selectMenuListByUserId(Menu menu);

    /**
     * 根据角色ID查询权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<String> selectMenuPermsByRoleId(Long roleId);

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<String> selectMenuPermsByUserId(Long userId);

    /**
     * 根据用户ID查询菜单
     *
     * @return 菜单列表
     */
    List<MenuDO> selectMenuTreeAll();

    /**
     * 根据用户ID查询菜单
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<MenuDO> selectMenuTreeByUserId(Long userId);

    /**
     * 根据角色ID查询菜单树信息
     *
     * @param roleId            角色ID
     * @return 选中菜单列表
     */
    List<Long> selectMenuListByRoleId(@Param("roleId") Long roleId);

    /**
     * 是否存在菜单子节点
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    int hasChildByMenuId(Long menuId);

    /**
     * 保存菜单排序
     *
     * @param menu 菜单信息
     */
    void updateMenuSort(MenuDO menu);

    /**
     * 校验菜单名称是否唯一
     *
     * @param menuName 菜单名称
     * @param parentId 父菜单ID
     * @param excludeMenuId 排除的菜单ID，新增时为空
     * @return 结果
     */
    MenuDO checkMenuNameUnique(@Param("menuName") String menuName, @Param("parentId") Long parentId,
                              @Param("excludeMenuId") Long excludeMenuId);

    /**
     * 根据路由路径或名称查询菜单信息（用于唯一性校验）
     *
     * @param path      路由地址
     * @param routeName 路由名称
     * @return 匹配的菜单列表
     */
    List<MenuDO> selectMenusByPathOrRouteName(@Param("path") String path, @Param("routeName") String routeName);
}
