package com.lemnt.system.adapter.web.auth;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.system.adapter.web.auth.req.*;
import com.lemnt.system.adapter.web.auth.res.*;
import com.lemnt.system.application.auth.executor.*;
import com.lemnt.system.application.org.executor.DeptTreeQueryExe;
import com.lemnt.system.application.org.qry.DeptListQry;
import com.lemnt.system.domain.auth.model.TreeSelect;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 用户部门选项接口。 */
@Tag(name = "用户部门选项")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserDeptController {
    private final DeptTreeQueryExe deptTreeQueryExe;

    @Operation(summary = "获取部门树列表（用于新增用户时选择部门）")
    @RequiresPermissions("system:user:list")
    @GetMapping("/dept-tree")
    public R<List<TreeSelect>> deptTree(DeptListQry qry) {
        return R.ok(deptTreeQueryExe.execute(qry));
    }
}
