package com.lemnt.system.adapter.web.log;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.core.domain.R;
import com.lemnt.common.log.annotation.Log;
import com.lemnt.common.log.enums.BusinessType;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.system.adapter.web.log.converter.OperLogReqConverter;
import com.lemnt.system.adapter.web.log.converter.OperLogResConverter;
import com.lemnt.system.adapter.web.log.req.OperLogPageReq;
import com.lemnt.system.adapter.web.log.res.OperLogRes;
import com.lemnt.system.application.log.dto.OperLogDTO;
import com.lemnt.system.application.log.executor.OperLogCleanCmdExe;
import com.lemnt.system.application.log.executor.OperLogDeleteCmdExe;
import com.lemnt.system.application.log.executor.OperLogPageQueryExe;
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
 * 操作日志记录
 *
 * @author wangxx
 */
@Tag(name = "操作日志")
@RestController
@RequestMapping("/operation-logs")
@RequiredArgsConstructor
public class OperlogController {

    private final OperLogPageQueryExe operLogPageQueryExe;
    private final OperLogDeleteCmdExe operLogDeleteCmdExe;
    private final OperLogCleanCmdExe operLogCleanCmdExe;

    @Operation(summary = "获取操作日志列表")
    @RequiresPermissions("system:operlog:list")
    @GetMapping
    public R<OperLogRes> list(@Validated OperLogPageReq req) {
        PageResult<OperLogDTO> result = operLogPageQueryExe.execute(OperLogReqConverter.toQry(req));
        return R.page(OperLogResConverter.toResList(result.getRecords()), result.getTotal());
    }

    @Operation(summary = "删除操作日志")
    @Log(title = "操作日志", businessType = BusinessType.DELETE)
    @RequiresPermissions("system:operlog:remove")
    @DeleteMapping("/{operIds}")
    public R<Boolean> remove(@PathVariable List<Long> operIds) {
        return R.ok(operLogDeleteCmdExe.execute(operIds));
    }

    @Operation(summary = "清空操作日志")
    @RequiresPermissions("system:operlog:remove")
    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public R<Boolean> clean() {
        return R.ok(operLogCleanCmdExe.execute());
    }
}
