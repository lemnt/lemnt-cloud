package com.lemnt.system.adapter.web.auth;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.system.adapter.web.auth.converter.TreeSelectResConverter;
import com.lemnt.system.adapter.web.auth.res.UserDeptTreeRes;
import com.lemnt.system.adapter.web.org.converter.DeptReqConverter;
import com.lemnt.system.adapter.web.org.req.DeptListReq;
import com.lemnt.system.application.org.executor.DeptTreeQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
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
    public R<List<UserDeptTreeRes>> deptTree(@Validated DeptListReq req) {
        return R.ok(
                TreeSelectResConverter.toUserDeptResList(
                        deptTreeQueryExe.execute(DeptReqConverter.toQry(req))));
    }
}
