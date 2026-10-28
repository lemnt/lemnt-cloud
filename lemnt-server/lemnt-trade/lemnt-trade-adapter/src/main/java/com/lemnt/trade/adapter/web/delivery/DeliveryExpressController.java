package com.lemnt.trade.adapter.web.delivery;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.delivery.converter.DeliveryReqConverter;
import com.lemnt.trade.adapter.web.delivery.converter.DeliveryResConverter;
import com.lemnt.trade.adapter.web.delivery.req.DeliveryExpressCreateReq;
import com.lemnt.trade.adapter.web.delivery.req.DeliveryExpressPageReq;
import com.lemnt.trade.adapter.web.delivery.req.DeliveryExpressUpdateReq;
import com.lemnt.trade.adapter.web.delivery.res.DeliveryExpressDetailRes;
import com.lemnt.trade.adapter.web.delivery.res.DeliveryExpressPageRes;
import com.lemnt.trade.adapter.web.delivery.res.DeliveryExpressSimpleRes;
import com.lemnt.trade.application.delivery.executor.DeliveryExpressCreateCmdExe;
import com.lemnt.trade.application.delivery.executor.DeliveryExpressDeleteCmdExe;
import com.lemnt.trade.application.delivery.executor.DeliveryExpressDetailQueryExe;
import com.lemnt.trade.application.delivery.executor.DeliveryExpressPageQueryExe;
import com.lemnt.trade.application.delivery.executor.DeliveryExpressSimpleListQueryExe;
import com.lemnt.trade.application.delivery.executor.DeliveryExpressUpdateCmdExe;

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
@Tag(name = "快递公司管理")
@RestController
@RequestMapping("/trade/admin/delivery/expresses")
@RequiredArgsConstructor
public class DeliveryExpressController {
    private final DeliveryExpressCreateCmdExe create;
    private final DeliveryExpressUpdateCmdExe update;
    private final DeliveryExpressDeleteCmdExe delete;
    private final DeliveryExpressDetailQueryExe detail;
    private final DeliveryExpressPageQueryExe page;
    private final DeliveryExpressSimpleListQueryExe simpleList;

    /** 配送新增操作。 */
    @PostMapping
    @Operation(summary = "配送新增")
    @RequiresPermissions("trade:delivery-express:create")
    public R<Long> create(@Valid @RequestBody DeliveryExpressCreateReq req) {
        return R.ok(create.execute(DeliveryReqConverter.deliveryExpressCreate(req)));
    }

    /** 配送修改操作。 */
    @PutMapping("/{id}")
    @Operation(summary = "配送修改")
    @RequiresPermissions("trade:delivery-express:update")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody DeliveryExpressUpdateReq req) {
        return R.ok(update.execute(DeliveryReqConverter.deliveryExpressUpdate(id, req)));
    }

    /** 配送删除操作。 */
    @DeleteMapping("/{id}")
    @Operation(summary = "配送删除")
    @RequiresPermissions("trade:delivery-express:delete")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(delete.execute(id));
    }

    /** 配送详情操作。 */
    @GetMapping("/{id}")
    @Operation(summary = "配送详情")
    @RequiresPermissions("trade:delivery-express:query")
    public R<DeliveryExpressDetailRes> detail(@PathVariable Long id) {
        return R.ok(DeliveryResConverter.deliveryExpressDetail(detail.execute(id)));
    }

    /** 配送分页操作。 */
    @GetMapping
    @Operation(summary = "配送分页")
    @RequiresPermissions("trade:delivery-express:list")
    public R<DeliveryExpressPageRes> page(@Valid @ModelAttribute DeliveryExpressPageReq req) {
        var result = page.execute(DeliveryReqConverter.deliveryExpressPage(req));
        return R.page(result.getRecords().stream().map(DeliveryResConverter::deliveryExpressPage).toList(), result.getTotal());
    }

    /** 配送选择列表操作。 */
    @GetMapping("/simple")
    @Operation(summary = "配送选择列表")
    @RequiresPermissions("trade:delivery-express:query")
    public R<List<DeliveryExpressSimpleRes>> simpleList() {
        return R.ok(simpleList.execute().stream().map(DeliveryResConverter::deliveryExpressSimple).toList());
    }
}
