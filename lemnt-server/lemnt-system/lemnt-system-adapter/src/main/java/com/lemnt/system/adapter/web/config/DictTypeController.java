package com.lemnt.system.adapter.web.config;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.log.annotation.Log;
import com.lemnt.common.log.enums.BusinessType;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.system.adapter.web.config.converter.DictTypeReqConverter;
import com.lemnt.system.adapter.web.config.converter.DictTypeResConverter;
import com.lemnt.system.adapter.web.config.req.DictTypeAddReq;
import com.lemnt.system.adapter.web.config.req.DictTypePageReq;
import com.lemnt.system.adapter.web.config.req.DictTypeUpdateReq;
import com.lemnt.system.adapter.web.config.res.SystemDictTypeGetInfoRes;
import com.lemnt.system.adapter.web.config.res.SystemDictTypeListRes;
import com.lemnt.system.adapter.web.config.res.SystemDictTypeOptionSelectRes;
import com.lemnt.system.application.config.dto.DictTypeDTO;
import com.lemnt.system.application.config.executor.*;

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
 * 数据字典信息操作处理
 *
 * @author wangxx
 */
@Tag(name = "字典类型")
@RestController
@RequestMapping("/dictionary-types")
@RequiredArgsConstructor
public class DictTypeController {
    private final DictTypePageQueryExe dictTypePageQueryExe;
    private final DictTypeListAllQueryExe dictTypeListAllQueryExe;
    private final DictTypeDetailQueryExe dictTypeDetailQueryExe;
    private final DictTypeAddCmdExe dictTypeAddCmdExe;
    private final DictTypeUpdateCmdExe dictTypeUpdateCmdExe;
    private final DictTypeDeleteCmdExe dictTypeDeleteCmdExe;
    private final DictTypeCacheRefreshCmdExe dictTypeCacheRefreshCmdExe;

    @Operation(summary = "查询字典类型列表")
    @RequiresPermissions("system:dict:list")
    @GetMapping
    public R<SystemDictTypeListRes> list(@Validated DictTypePageReq req) {
        PageResult<DictTypeDTO> result = dictTypePageQueryExe.execute(DictTypeReqConverter.toQry(req));
        return R.page(DictTypeResConverter.toResListToSystemDictTypeListRes(result.getRecords()), result.getTotal());
    }

    @Operation(summary = "查询字典类型详细")
    @GetMapping(value = "/{dictId}")
    public R<SystemDictTypeGetInfoRes> getInfo(@PathVariable Long dictId) {
        return R.ok(DictTypeResConverter.toResToSystemDictTypeGetInfoRes(dictTypeDetailQueryExe.executeById(dictId)));
    }

    @Operation(summary = "根据字典类型查询字典数据信息")
    @GetMapping(value = "/options")
    public R<List<SystemDictTypeOptionSelectRes>> optionSelect() {
        return R.ok(DictTypeResConverter.toResListToSystemDictTypeOptionSelectRes(dictTypeListAllQueryExe.execute()));
    }

    @Operation(summary = "新增字典类型")
    @RequiresPermissions("system:dict:add")
    @Log(title = "字典类型", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Boolean> add(@Validated @RequestBody DictTypeAddReq req) {
        return R.ok(dictTypeAddCmdExe.execute(DictTypeReqConverter.toCmd(req)));
    }

    @Operation(summary = "修改字典类型")
    @RequiresPermissions("system:dict:edit")
    @Log(title = "字典类型", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Boolean> edit(@Validated @RequestBody DictTypeUpdateReq req) {
        return R.ok(dictTypeUpdateCmdExe.execute(DictTypeReqConverter.toCmd(req)));
    }

    @Operation(summary = "删除字典类型")
    @RequiresPermissions("system:dict:remove")
    @Log(title = "字典类型", businessType = BusinessType.DELETE)
    @DeleteMapping("/{dictIds}")
    public R<Boolean> remove(@PathVariable List<Long> dictIds) {
        return R.ok(dictTypeDeleteCmdExe.execute(dictIds));
    }

    @Operation(summary = "刷新字典缓存")
    @RequiresPermissions("system:dict:remove")
    @Log(title = "字典类型", businessType = BusinessType.CLEAN)
    @DeleteMapping("/refresh-cache")
    public R<Boolean> refreshCache() {
        return R.ok(dictTypeCacheRefreshCmdExe.execute());
    }
}
