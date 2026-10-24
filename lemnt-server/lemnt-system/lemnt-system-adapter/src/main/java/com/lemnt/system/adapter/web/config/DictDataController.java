package com.lemnt.system.adapter.web.config;

import java.util.List;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.application.config.executor.*;
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
import com.lemnt.common.core.domain.R;
import com.lemnt.common.log.annotation.Log;
import com.lemnt.common.log.enums.BusinessType;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.system.adapter.web.config.converter.DictDataReqConverter;
import com.lemnt.system.adapter.web.config.converter.DictDataResConverter;
import com.lemnt.system.adapter.web.config.req.DictDataAddReq;
import com.lemnt.system.adapter.web.config.req.DictDataUpdateReq;
import com.lemnt.system.adapter.web.config.res.DictDataRes;
import com.lemnt.system.application.config.dto.DictDataDTO;
import com.lemnt.system.application.config.qry.DictDataPageQry;

/**
 * 数据字典数据信息操作处理
 *
 * @author wangxx
 */
@Tag(name = "字典数据")
@RestController
@RequestMapping("/dictionary-data")
@RequiredArgsConstructor
public class DictDataController {

    private final DictDataPageQueryExe dictDataPageQueryExe;
    private final DictDataDetailQueryExe dictDataDetailQueryExe;
    private final DictDataAddCmdExe dictDataAddCmdExe;
    private final DictDataUpdateCmdExe dictDataUpdateCmdExe;
    private final DictDataDeleteCmdExe dictDataDeleteCmdExe;
    private final DictDataListQueryExe dictDataListQueryExe;
    ;

    @Operation(summary = "查询字典数据列表")
    @RequiresPermissions("system:dict:list")
    @GetMapping
    public R<DictDataRes> list(DictDataPageQry qry) {
        PageResult<DictDataDTO> result = dictDataPageQueryExe.execute(qry);
        return R.page(DictDataResConverter.toResList(result.getRecords()), result.getTotal());
    }

    @Operation(summary = "查询字典数据详细")
    @GetMapping(value = "/{dictCode}")
    public R<DictDataRes> getInfo(@PathVariable Long dictCode) {
        return R.ok(DictDataResConverter.toRes(dictDataDetailQueryExe.execute(dictCode)));
    }

    @Operation(summary = "根据字典类型查询字典数据信息")
    @GetMapping(value = "/type/{dictType}")
    public R<List<DictDataRes>> dictType(@PathVariable String dictType) {
        return R.ok(DictDataResConverter.toResList(dictDataListQueryExe.execute(dictType)));
    }

    @Operation(summary = "新增字典数据")
    @RequiresPermissions("system:dict:add")
    @Log(title = "字典数据", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Boolean> add(@Validated @RequestBody DictDataAddReq req) {
        return R.ok(dictDataAddCmdExe.execute(DictDataReqConverter.toAddCmd(req)));
    }

    @Operation(summary = "修改字典数据")
    @RequiresPermissions("system:dict:edit")
    @Log(title = "字典数据", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Boolean> edit(@Validated @RequestBody DictDataUpdateReq req) {
        return R.ok(dictDataUpdateCmdExe.execute(DictDataReqConverter.toUpdateCmd(req)));
    }

    @Operation(summary = "删除字典数据")
    @RequiresPermissions("system:dict:remove")
    @Log(title = "字典数据", businessType = BusinessType.DELETE)
    @DeleteMapping("/{dictCodes}")
    public R<Boolean> remove(@PathVariable List<Long> dictCodes) {
        return R.ok(dictDataDeleteCmdExe.execute(dictCodes));
    }
}
