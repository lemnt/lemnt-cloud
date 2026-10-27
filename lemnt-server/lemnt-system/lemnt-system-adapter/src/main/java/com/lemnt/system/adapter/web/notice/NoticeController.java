package com.lemnt.system.adapter.web.notice;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.log.annotation.Log;
import com.lemnt.common.log.enums.BusinessType;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.adapter.web.notice.converter.NoticeReqConverter;
import com.lemnt.system.adapter.web.notice.converter.NoticeResConverter;
import com.lemnt.system.adapter.web.notice.req.*;
import com.lemnt.system.adapter.web.notice.res.NoticeTopRes;
import com.lemnt.system.adapter.web.notice.res.SystemNoticeGetInfoRes;
import com.lemnt.system.adapter.web.notice.res.SystemNoticeListRes;
import com.lemnt.system.application.notice.dto.NoticeDTO;
import com.lemnt.system.application.notice.executor.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

/** 通知公告接口。 */
@Tag(name = "通知公告")
@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {
    private final NoticePageQueryExe noticePageQueryExe;
    private final NoticeDetailQueryExe noticeDetailQueryExe;
    private final NoticeAddCmdExe noticeAddCmdExe;
    private final NoticeUpdateCmdExe noticeUpdateCmdExe;
    private final NoticeDeleteCmdExe noticeDeleteCmdExe;
    private final NoticeTopListQueryExe noticeTopListQueryExe;

    @Operation(summary = "获取通知公告列表")
    @RequiresPermissions("system:notice:list")
    @GetMapping
    public R<SystemNoticeListRes> list(@Validated NoticePageReq req) {
        PageResult<NoticeDTO> result = noticePageQueryExe.execute(NoticeReqConverter.toQry(req));
        return R.page(NoticeResConverter.toResListToSystemNoticeListRes(result.getRecords()), result.getTotal());
    }

    @Operation(summary = "根据通知公告编号获取详细信息")
    @GetMapping(value = "/{noticeId}")
    public R<SystemNoticeGetInfoRes> getInfo(@PathVariable Long noticeId) {
        return R.ok(NoticeResConverter.toResToSystemNoticeGetInfoRes(noticeDetailQueryExe.execute(noticeId)));
    }

    @Operation(summary = "首页顶部公告列表")
    @SuppressWarnings("unchecked")
    @GetMapping("/top")
    public R<NoticeTopRes> listTop() {
        Long userId = SecurityUtils.getUserId();
        Map<String, Object> result = noticeTopListQueryExe.execute(userId, 5);
        NoticeTopRes res = new NoticeTopRes(
                NoticeResConverter.toResList((List<NoticeDTO>) result.get("list")),
                (Integer) result.get("unreadCount"));
        return R.ok(res);
    }

    @Operation(summary = "新增通知公告")
    @RequiresPermissions("system:notice:add")
    @Log(title = "通知公告", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Boolean> add(@Validated @RequestBody NoticeAddReq req) {
        return R.ok(noticeAddCmdExe.execute(NoticeReqConverter.toCmd(req)));
    }

    @Operation(summary = "修改通知公告")
    @RequiresPermissions("system:notice:edit")
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Boolean> edit(@Validated @RequestBody NoticeUpdateReq req) {
        return R.ok(noticeUpdateCmdExe.execute(NoticeReqConverter.toCmd(req)));
    }

    @Operation(summary = "删除通知公告")
    @RequiresPermissions("system:notice:remove")
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeIds}")
    public R<Boolean> remove(@PathVariable List<Long> noticeIds) {
        return R.ok(noticeDeleteCmdExe.execute(noticeIds));
    }
}
