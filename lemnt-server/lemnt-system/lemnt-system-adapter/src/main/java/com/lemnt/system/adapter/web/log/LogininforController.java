package com.lemnt.system.adapter.web.log;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.core.domain.R;
import com.lemnt.common.log.annotation.Log;
import com.lemnt.common.log.enums.BusinessType;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.system.adapter.web.log.converter.LogininforReqConverter;
import com.lemnt.system.adapter.web.log.converter.LogininforResConverter;
import com.lemnt.system.adapter.web.log.req.LogininforPageReq;
import com.lemnt.system.adapter.web.log.res.LogininforRes;
import com.lemnt.system.application.log.dto.LogininforDTO;
import com.lemnt.system.application.log.executor.LogininforCleanCmdExe;
import com.lemnt.system.application.log.executor.LogininforDeleteCmdExe;
import com.lemnt.system.application.log.executor.LogininforPageQueryExe;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统访问记录
 *
 * @author wangxx
 */
@Tag(name = "登录日志")
@RestController
@RequestMapping("/login-logs")
@RequiredArgsConstructor
public class LogininforController {

    private final LogininforPageQueryExe logininforPageQueryExe;
    private final LogininforDeleteCmdExe logininforDeleteCmdExe;
    private final LogininforCleanCmdExe logininforCleanCmdExe;

    @Operation(summary = "获取登录日志列表")
    @RequiresPermissions("system:logininfor:list")
    @GetMapping
    public R<LogininforRes> page(@Validated LogininforPageReq req) {
        PageResult<LogininforDTO> result = logininforPageQueryExe.execute(LogininforReqConverter.toQry(req));
        return R.page(LogininforResConverter.toResList(result.getRecords()), result.getTotal());
    }

    @Operation(summary = "删除登录日志")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @RequiresPermissions("system:logininfor:remove")
    @DeleteMapping("/{infoIds}")
    public R<Boolean> remove(@PathVariable List<Long> infoIds) {
        return R.ok(logininforDeleteCmdExe.execute(infoIds));
    }

    @Operation(summary = "清空登录日志")
    @RequiresPermissions("system:logininfor:remove")
    @Log(title = "登录日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public R<Boolean> clean() {
        return R.ok(logininforCleanCmdExe.execute());
    }
}
