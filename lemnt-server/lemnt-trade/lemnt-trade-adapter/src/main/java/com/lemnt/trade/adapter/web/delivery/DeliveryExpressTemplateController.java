package com.lemnt.trade.adapter.web.delivery;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.delivery.converter.DeliveryReqConverter;
import com.lemnt.trade.adapter.web.delivery.converter.DeliveryResConverter;
import com.lemnt.trade.adapter.web.delivery.req.DeliveryExpressTemplateCreateReq;
import com.lemnt.trade.adapter.web.delivery.req.DeliveryExpressTemplatePageReq;
import com.lemnt.trade.adapter.web.delivery.req.DeliveryExpressTemplateUpdateReq;
import com.lemnt.trade.adapter.web.delivery.res.DeliveryExpressTemplateDetailRes;
import com.lemnt.trade.adapter.web.delivery.res.DeliveryExpressTemplatePageRes;
import com.lemnt.trade.adapter.web.delivery.res.DeliveryExpressTemplateSimpleRes;
import com.lemnt.trade.application.delivery.executor.DeliveryExpressTemplateCreateCmdExe;
import com.lemnt.trade.application.delivery.executor.DeliveryExpressTemplateDeleteCmdExe;
import com.lemnt.trade.application.delivery.executor.DeliveryExpressTemplateDetailQueryExe;
import com.lemnt.trade.application.delivery.executor.DeliveryExpressTemplatePageQueryExe;
import com.lemnt.trade.application.delivery.executor.DeliveryExpressTemplateSimpleListQueryExe;
import com.lemnt.trade.application.delivery.executor.DeliveryExpressTemplateUpdateCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import java.util.List;

/** 配送管理接口。 */
@Tag(name = "运费模板管理")
@RestController
@RequestMapping("/trade/admin/delivery/templates")
@RequiredArgsConstructor
public class DeliveryExpressTemplateController {
    private final DeliveryExpressTemplateCreateCmdExe create;
    private final DeliveryExpressTemplateUpdateCmdExe update;
    private final DeliveryExpressTemplateDeleteCmdExe delete;
    private final DeliveryExpressTemplateDetailQueryExe detail;
    private final DeliveryExpressTemplatePageQueryExe page;
    private final DeliveryExpressTemplateSimpleListQueryExe simpleList;

    /** 配送新增操作。 */
    @PostMapping
    @Operation(summary = "配送新增")
    @RequiresPermissions("trade:delivery-template:create")
    public R<Long> create(@Valid @RequestBody DeliveryExpressTemplateCreateReq req) {
        return R.ok(create.execute(DeliveryReqConverter.deliveryExpressTemplateCreate(req)));
    }

    /** 配送修改操作。 */
    @PutMapping("/{id}")
    @Operation(summary = "配送修改")
    @RequiresPermissions("trade:delivery-template:update")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody DeliveryExpressTemplateUpdateReq req) {
        return R.ok(update.execute(DeliveryReqConverter.deliveryExpressTemplateUpdate(id, req)));
    }

    /** 配送删除操作。 */
    @DeleteMapping("/{id}")
    @Operation(summary = "配送删除")
    @RequiresPermissions("trade:delivery-template:delete")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(delete.execute(id));
    }

    /** 配送详情操作。 */
    @GetMapping("/{id}")
    @Operation(summary = "配送详情")
    @RequiresPermissions("trade:delivery-template:query")
    public R<DeliveryExpressTemplateDetailRes> detail(@PathVariable Long id) {
        return R.ok(DeliveryResConverter.deliveryExpressTemplateDetail(detail.execute(id)));
    }

    /** 配送分页操作。 */
    @GetMapping
    @Operation(summary = "配送分页")
    @RequiresPermissions("trade:delivery-template:list")
    public R<DeliveryExpressTemplatePageRes> page(@Valid @ModelAttribute DeliveryExpressTemplatePageReq req) {
        var result = page.execute(DeliveryReqConverter.deliveryExpressTemplatePage(req));
        return R.page(result.getRecords().stream().map(DeliveryResConverter::deliveryExpressTemplatePage).toList(), result.getTotal());
    }

    /** 配送选择列表操作。 */
    @GetMapping("/simple")
    @Operation(summary = "配送选择列表")
    @RequiresPermissions("trade:delivery-template:query")
    public R<List<DeliveryExpressTemplateSimpleRes>> simpleList() {
        return R.ok(simpleList.execute().stream().map(DeliveryResConverter::deliveryExpressTemplateSimple).toList());
    }
}
