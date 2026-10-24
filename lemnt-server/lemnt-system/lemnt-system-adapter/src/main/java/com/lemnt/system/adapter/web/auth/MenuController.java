package com.lemnt.system.adapter.web.auth;

import java.util.List;
import java.util.Map;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.log.annotation.Log;
import com.lemnt.common.log.enums.BusinessType;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.adapter.web.auth.converter.MenuReqConverter;
import com.lemnt.system.adapter.web.auth.converter.MenuResConverter;
import com.lemnt.system.adapter.web.auth.converter.TreeSelectResConverter;
import com.lemnt.system.adapter.web.auth.req.MenuAddReq;
import com.lemnt.system.adapter.web.auth.req.MenuListReq;
import com.lemnt.system.adapter.web.auth.req.MenuUpdateReq;
import com.lemnt.system.adapter.web.auth.res.MenuRes;
import com.lemnt.system.adapter.web.auth.res.RoleMenuTreeListRes;
import com.lemnt.system.adapter.web.auth.res.RouterRes;
import com.lemnt.system.adapter.web.auth.res.TreeSelectRes;
import com.lemnt.system.application.auth.executor.*;
import com.lemnt.system.domain.auth.model.TreeSelect;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 菜单信息操作处理
 *
 * @author wangxx
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuListQueryExe menuListQueryExe;
    private final MenuDetailQueryExe menuDetailQueryExe;
    private final MenuUserRouterQryExe menuUserRouterQryExe;
    private final MenuAddCmdExe menuAddCmdExe;
    private final MenuUpdateCmdExe menuUpdateCmdExe;
    private final MenuDeleteCmdExe menuDeleteCmdExe;
    private final MenuRoleTreeQueryExe menuRoleTreeQueryExe;
    private final MenuTreeSelectQueryExe menuTreeSelectQueryExe;

    @Operation(summary = "获取菜单列表")
    @RequiresPermissions("system:menu:list")
    @GetMapping
    public R<List<MenuRes>> list(@Validated MenuListReq req) {
        return R.ok(MenuResConverter.toResList(
                menuListQueryExe.execute(MenuReqConverter.toQry(req, SecurityUtils.getUserId()))));
    }

    @Operation(summary = "根据菜单编号获取详细信息")
    @RequiresPermissions("system:menu:query")
    @GetMapping(value = "/{menuId}")
    public R<MenuRes> getInfo(@PathVariable Long menuId) {
        return R.ok(MenuResConverter.toRes(menuDetailQueryExe.execute(menuId)));
    }

    @Operation(summary = "获取菜单下拉树列表")
    @GetMapping("/tree-select")
    public R<List<TreeSelectRes>> treeSelect(@Validated MenuListReq req) {
        return R.ok(TreeSelectResConverter.toResList(
                menuTreeSelectQueryExe.execute(MenuReqConverter.toQry(req, SecurityUtils.getUserId()))));
    }

    @Operation(summary = "加载对应角色菜单列表树")
    @RequiresPermissions("system:role:query")

    @SuppressWarnings("unchecked")
    @GetMapping(value = "/role-menu-tree-select/{roleId}")
    public R<RoleMenuTreeListRes> roleMenuTreeSelect(@PathVariable("roleId") Long roleId) {
        Map<String, Object> result = menuRoleTreeQueryExe.execute(roleId);
        RoleMenuTreeListRes res = new RoleMenuTreeListRes(
                (List<Long>) result.get("checkedKeys"),
                TreeSelectResConverter.toResList((List<TreeSelect>) result.get("menus")));
        return R.ok(res);
    }

    @Operation(summary = "获取路由信息")
    @GetMapping("/routers")
    public R<List<RouterRes>> getRouters() {
        Long userId = SecurityUtils.getUserId();
        return R.ok(MenuResConverter.toRouterResList(menuUserRouterQryExe.execute(userId)));
    }

    @Operation(summary = "新增菜单")
    @RequiresPermissions("system:menu:add")
    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Boolean> add(@Validated @RequestBody MenuAddReq req) {
        return R.ok(menuAddCmdExe.execute(MenuReqConverter.toCmd(req)));
    }

    @Operation(summary = "修改菜单")
    @RequiresPermissions("system:menu:edit")
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Boolean> edit(@Validated @RequestBody MenuUpdateReq req) {
        return R.ok(menuUpdateCmdExe.execute(MenuReqConverter.toCmd(req)));
    }

    @Operation(summary = "删除菜单")
    @RequiresPermissions("system:menu:remove")
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{menuId}")
    public R<Boolean> remove(@PathVariable("menuId") Long menuId) {
        return R.ok(menuDeleteCmdExe.execute(menuId));
    }
}
