package com.lemnt.system.adapter.web.config;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.log.annotation.Log;
import com.lemnt.common.log.enums.BusinessType;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.system.adapter.web.config.converter.ConfigReqConverter;
import com.lemnt.system.adapter.web.config.converter.ConfigResConverter;
import com.lemnt.system.adapter.web.config.req.ConfigAddReq;
import com.lemnt.system.adapter.web.config.req.ConfigPageReq;
import com.lemnt.system.adapter.web.config.req.ConfigUpdateReq;
import com.lemnt.system.adapter.web.config.res.SystemConfigGetConfigKeyRes;
import com.lemnt.system.adapter.web.config.res.SystemConfigGetInfoRes;
import com.lemnt.system.adapter.web.config.res.SystemConfigListRes;
import com.lemnt.system.application.config.dto.ConfigDTO;
import com.lemnt.system.application.config.executor.ConfigAddCmdExe;
import com.lemnt.system.application.config.executor.ConfigCacheRefreshCmdExe;
import com.lemnt.system.application.config.executor.ConfigDeleteCmdExe;
import com.lemnt.system.application.config.executor.ConfigDetailQueryExe;
import com.lemnt.system.application.config.executor.ConfigPageQueryExe;
import com.lemnt.system.application.config.executor.ConfigUpdateCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 参数配置 信息操作处理
 *
 * @author wangxx
 */
@Tag(name = "参数配置")
@RestController
@RequestMapping("/configurations")
@RequiredArgsConstructor
public class ConfigController {
    private final ConfigPageQueryExe configPageQueryExe;
    private final ConfigDetailQueryExe configDetailQueryExe;
    private final ConfigAddCmdExe configAddCmdExe;
    private final ConfigUpdateCmdExe configUpdateCmdExe;
    private final ConfigDeleteCmdExe configDeleteCmdExe;
    private final ConfigCacheRefreshCmdExe configCacheRefreshCmdExe;

    @Operation(summary = "获取参数配置列表")
    @RequiresPermissions("system:config:list")
    @GetMapping
    public R<SystemConfigListRes> list(ConfigPageReq req) {
        PageResult<ConfigDTO> result = configPageQueryExe.execute(ConfigReqConverter.toQry(req));
        return R.page(ConfigResConverter.toResListToSystemConfigListRes(result.getRecords()), result.getTotal());
    }

    @Operation(summary = "根据参数编号获取详细信息")
    @RequiresPermissions("system:config:query")
    @GetMapping(value = "/{configId}")
    public R<SystemConfigGetInfoRes> getInfo(@PathVariable Long configId) {
        return R.ok(ConfigResConverter.toResToSystemConfigGetInfoRes(configDetailQueryExe.executeById(configId)));
    }

    @Operation(summary = "根据参数键名查询参数值")
    @RequiresPermissions("system:config:query")
    @GetMapping(value = "/config-key/{configKey}")
    public R<SystemConfigGetConfigKeyRes> getConfigKey(@PathVariable String configKey) {
        return R.ok(ConfigResConverter.toResToSystemConfigGetConfigKeyRes(configDetailQueryExe.executeByKey(configKey)));
    }

    @Operation(summary = "新增参数配置")
    @RequiresPermissions("system:config:add")
    @Log(title = "参数管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Boolean> add(@Validated @RequestBody ConfigAddReq req) {
        return R.ok(configAddCmdExe.execute(ConfigReqConverter.toAddCmd(req)));
    }

    @Operation(summary = "修改参数配置")
    @RequiresPermissions("system:config:edit")
    @Log(title = "参数管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Boolean> edit(@Validated @RequestBody ConfigUpdateReq req) {
        return R.ok(configUpdateCmdExe.execute(ConfigReqConverter.toUpdateCmd(req)));
    }

    @Operation(summary = "删除参数配置")
    @RequiresPermissions("system:config:remove")
    @Log(title = "参数管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public R<Boolean> remove(@PathVariable List<Long> configIds) {
        return R.ok(configDeleteCmdExe.execute(configIds));
    }

    @Operation(summary = "刷新参数缓存")
    @RequiresPermissions("system:config:remove")
    @Log(title = "参数管理", businessType = BusinessType.CLEAN)
    @DeleteMapping("/refresh-cache")
    public R<Boolean> refreshCache() {
        return R.ok(configCacheRefreshCmdExe.execute());
    }
}
