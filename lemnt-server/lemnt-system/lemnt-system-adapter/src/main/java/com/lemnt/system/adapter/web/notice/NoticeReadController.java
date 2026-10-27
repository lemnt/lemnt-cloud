package com.lemnt.system.adapter.web.notice;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.adapter.web.notice.converter.NoticeReadResConverter;
import com.lemnt.system.adapter.web.notice.converter.NoticeReqConverter;
import com.lemnt.system.adapter.web.notice.req.*;
import com.lemnt.system.adapter.web.notice.res.NoticeReadUserRes;
import com.lemnt.system.application.notice.dto.NoticeReadUserDTO;
import com.lemnt.system.application.notice.executor.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

/** 公告阅读接口。 */
@Tag(name = "公告阅读")
@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeReadController {
    private final NoticeReadUserPageQueryExe noticeReadUserPageQueryExe;
    private final NoticeMarkReadCmdExe noticeMarkReadCmdExe;
    private final NoticeMarkReadAllCmdExe noticeMarkReadAllCmdExe;

    @Operation(summary = "已读用户列表数据")
    @RequiresPermissions("system:notice:list")
    @GetMapping("/read-users")
    public R<NoticeReadUserRes> readUsersList(NoticeReadUsersPageReq req) {
        PageResult<NoticeReadUserDTO> result = noticeReadUserPageQueryExe.execute(NoticeReqConverter.toQry(req));
        return R.page(NoticeReadResConverter.toResList(result.getRecords()), result.getTotal());
    }

    @Operation(summary = "标记公告已读")
    @PostMapping("/mark-read")
    public R<Boolean> markRead(@RequestParam Long noticeId) {
        Long userId = SecurityUtils.getUserId();
        return R.ok(noticeMarkReadCmdExe.execute(noticeId, userId));
    }

    @Operation(summary = "批量标记已读")
    @PostMapping("/mark-read-all")
    public R<Boolean> markReadAll(@RequestBody NoticeMarkReadAllReq req) {
        Long userId = SecurityUtils.getUserId();
        return R.ok(noticeMarkReadAllCmdExe.execute(userId, req.ids()));
    }
}
