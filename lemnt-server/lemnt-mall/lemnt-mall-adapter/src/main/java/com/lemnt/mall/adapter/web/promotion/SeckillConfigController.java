package com.lemnt.mall.adapter.web.promotion;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.promotion.converter.SeckillConfigConverter;
import com.lemnt.mall.adapter.web.promotion.req.SeckillConfigCreateReq;
import com.lemnt.mall.adapter.web.promotion.req.SeckillConfigPageReq;
import com.lemnt.mall.adapter.web.promotion.req.SeckillConfigStatusReq;
import com.lemnt.mall.adapter.web.promotion.req.SeckillConfigUpdateReq;
import com.lemnt.mall.adapter.web.promotion.res.SeckillConfigRes;
import com.lemnt.mall.adapter.web.promotion.res.SeckillConfigSimpleRes;
import com.lemnt.mall.application.promotion.executor.SeckillConfigCreateCmdExe;
import com.lemnt.mall.application.promotion.executor.SeckillConfigDeleteCmdExe;
import com.lemnt.mall.application.promotion.executor.SeckillConfigDetailQueryExe;
import com.lemnt.mall.application.promotion.executor.SeckillConfigListQueryExe;
import com.lemnt.mall.application.promotion.executor.SeckillConfigPageQueryExe;
import com.lemnt.mall.application.promotion.executor.SeckillConfigSimpleListQueryExe;
import com.lemnt.mall.application.promotion.executor.SeckillConfigStatusCmdExe;
import com.lemnt.mall.application.promotion.executor.SeckillConfigUpdateCmdExe;

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

/** 秒杀时段管理入口。 */
@RestController
@RequestMapping("/mall/promotion/seckill-configs")
@Tag(name = "秒杀时段")
@RequiredArgsConstructor
public class SeckillConfigController {
    private final SeckillConfigCreateCmdExe create;
    private final SeckillConfigUpdateCmdExe update;
    private final SeckillConfigStatusCmdExe status;
    private final SeckillConfigDeleteCmdExe delete;
    private final SeckillConfigDetailQueryExe detail;
    private final SeckillConfigPageQueryExe page;
    private final SeckillConfigListQueryExe list;
    private final SeckillConfigSimpleListQueryExe simpleList;

    @PostMapping
    @Operation(summary = "创建秒杀时段")
    @RequiresPermissions("mall:promotion:create")
    public R<Long> create(@Valid @RequestBody SeckillConfigCreateReq req) {
        return R.ok(create.execute(SeckillConfigConverter.create(req)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改秒杀时段")
    @RequiresPermissions("mall:promotion:update")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody SeckillConfigUpdateReq req) {
        return R.ok(update.execute(SeckillConfigConverter.update(id, req)));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "修改秒杀时段状态")
    @RequiresPermissions("mall:promotion:update")
    public R<Boolean> status(@PathVariable Long id, @Valid @RequestBody SeckillConfigStatusReq req) {
        return R.ok(status.execute(id, req.status()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除秒杀时段")
    @RequiresPermissions("mall:promotion:delete")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(delete.execute(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "秒杀时段详情")
    @RequiresPermissions("mall:promotion:query")
    public R<SeckillConfigRes> detail(@PathVariable Long id) {
        return R.ok(SeckillConfigConverter.res(detail.execute(id)));
    }

    @GetMapping
    @Operation(summary = "秒杀时段分页")
    @RequiresPermissions("mall:promotion:list")
    public R<SeckillConfigRes> page(@Valid @ModelAttribute SeckillConfigPageReq req) {
        var result = page.execute(SeckillConfigConverter.page(req));
        return R.page(result.getRecords().stream().map(SeckillConfigConverter::res).toList(), result.getTotal());
    }

    @GetMapping("/list")
    @Operation(summary = "全部秒杀时段列表")
    @RequiresPermissions("mall:promotion:query")
    public R<List<SeckillConfigRes>> list() {
        return R.ok(list.execute().stream().map(SeckillConfigConverter::res).toList());
    }

    @GetMapping("/simple-list")
    @Operation(summary = "开启状态秒杀时段精简列表")
    public R<List<SeckillConfigSimpleRes>> simpleList() {
        return R.ok(simpleList.execute().stream().map(SeckillConfigConverter::simple).toList());
    }
}
