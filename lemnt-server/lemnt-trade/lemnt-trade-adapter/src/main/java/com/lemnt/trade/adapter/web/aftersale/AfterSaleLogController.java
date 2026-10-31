package com.lemnt.trade.adapter.web.aftersale;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.aftersale.converter.AfterSaleLogResConverter;
import com.lemnt.trade.adapter.web.aftersale.res.AfterSaleLogRes;
import com.lemnt.trade.application.aftersale.executor.AfterSaleLogListQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 后台售后日志查询。 */
@Tag(name = "售后操作日志")
@RestController
@RequiredArgsConstructor
@RequestMapping("/trade/admin/after-sales")
public class AfterSaleLogController {
    private final AfterSaleLogListQueryExe logs;
    /** 与售后详情使用同一查询权限。 */
    @GetMapping("/{no}/logs")
    @Operation(summary = "查询售后操作日志")
    @RequiresPermissions("trade:after-sale:query")
    public R<List<AfterSaleLogRes>> logs(@PathVariable String no) {
        return R.ok(logs.execute(no).stream().map(AfterSaleLogResConverter::response).toList());
    }
}
