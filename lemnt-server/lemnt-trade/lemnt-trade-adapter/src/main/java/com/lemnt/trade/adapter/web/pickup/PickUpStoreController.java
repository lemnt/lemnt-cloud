package com.lemnt.trade.adapter.web.pickup;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.pickup.converter.PickUpConverter;
import com.lemnt.trade.adapter.web.pickup.req.PickUpBindReq;
import com.lemnt.trade.adapter.web.pickup.req.PickUpStorePageReq;
import com.lemnt.trade.adapter.web.pickup.req.PickUpStoreSaveReq;
import com.lemnt.trade.adapter.web.pickup.res.PickUpStoreRes;
import com.lemnt.trade.application.pickup.cmd.PickUpBindCmd;
import com.lemnt.trade.application.pickup.executor.PickUpStoreBindCmdExe;
import com.lemnt.trade.application.pickup.executor.PickUpStoreCreateCmdExe;
import com.lemnt.trade.application.pickup.executor.PickUpStoreDeleteCmdExe;
import com.lemnt.trade.application.pickup.executor.PickUpStoreDetailQueryExe;
import com.lemnt.trade.application.pickup.executor.PickUpStorePageQueryExe;
import com.lemnt.trade.application.pickup.executor.PickUpStoreUpdateCmdExe;

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

/** 后台自提门店管理。 */
@Tag(name = "自提门店")
@RestController
@RequestMapping("/trade/admin/delivery/pick-up-stores")
@RequiredArgsConstructor
public class PickUpStoreController {
    private final PickUpStoreCreateCmdExe create;
    private final PickUpStoreUpdateCmdExe update;
    private final PickUpStoreBindCmdExe bind;
    private final PickUpStoreDeleteCmdExe delete;
    private final PickUpStoreDetailQueryExe detail;
    private final PickUpStorePageQueryExe page;

    @GetMapping
    @Operation(summary = "自提门店分页")
    @RequiresPermissions("trade:pick-up-store:list")
    public R<PickUpStoreRes> page(@Valid @ModelAttribute PickUpStorePageReq req) {
        var result = page.execute(PickUpConverter.page(req));
        return R.page(result.getRecords().stream().map(PickUpConverter::store).toList(), result.getTotal());
    }

    @GetMapping("/{id}")
    @Operation(summary = "自提门店详情")
    @RequiresPermissions("trade:pick-up-store:query")
    public R<PickUpStoreRes> detail(@PathVariable Long id) {
        return R.ok(PickUpConverter.store(detail.execute(id)));
    }

    @PostMapping
    @Operation(summary = "新增自提门店")
    @RequiresPermissions("trade:pick-up-store:create")
    public R<Long> create(@Valid @RequestBody PickUpStoreSaveReq req) {
        return R.ok(create.execute(PickUpConverter.store(req)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改自提门店")
    @RequiresPermissions("trade:pick-up-store:update")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody PickUpStoreSaveReq req) {
        return R.ok(update.execute(PickUpConverter.store(id, req)));
    }

    @PutMapping("/{id}/verify-users")
    @Operation(summary = "绑定额外核销店员")
    @RequiresPermissions("trade:pick-up-store:update")
    public R<Boolean> bind(@PathVariable Long id, @Valid @RequestBody PickUpBindReq req) {
        return R.ok(bind.execute(new PickUpBindCmd(id, req.verifyUserIds())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除自提门店")
    @RequiresPermissions("trade:pick-up-store:delete")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(delete.execute(id));
    }
}
