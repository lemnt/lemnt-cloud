package com.lemnt.system.adapter.web.org;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.log.annotation.Log;
import com.lemnt.common.log.enums.BusinessType;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.system.adapter.web.org.converter.PostReqConverter;
import com.lemnt.system.adapter.web.org.converter.PostResConverter;
import com.lemnt.system.adapter.web.org.req.PostAddReq;
import com.lemnt.system.adapter.web.org.req.PostPageReq;
import com.lemnt.system.adapter.web.org.req.PostUpdateReq;
import com.lemnt.system.adapter.web.org.res.SystemPostGetInfoRes;
import com.lemnt.system.adapter.web.org.res.SystemPostListRes;
import com.lemnt.system.application.org.dto.PostDTO;
import com.lemnt.system.application.org.executor.PostAddCmdExe;
import com.lemnt.system.application.org.executor.PostDeleteCmdExe;
import com.lemnt.system.application.org.executor.PostDetailQueryExe;
import com.lemnt.system.application.org.executor.PostPageQueryExe;
import com.lemnt.system.application.org.executor.PostUpdateCmdExe;

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
 * 岗位信息操作处理
 *
 * @author wangxx
 */
@Tag(name = "岗位管理")
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostPageQueryExe postPageQueryExe;
    private final PostDetailQueryExe postDetailQueryExe;
    private final PostAddCmdExe postAddCmdExe;
    private final PostUpdateCmdExe postUpdateCmdExe;
    private final PostDeleteCmdExe postDeleteCmdExe;

    @Operation(summary = "获取岗位列表")
    @RequiresPermissions("system:post:list")
    @GetMapping
    public R<SystemPostListRes> list(@Validated PostPageReq req) {
        PageResult<PostDTO> result = postPageQueryExe.execute(PostReqConverter.toQry(req));
        return R.page(PostResConverter.toResListToSystemPostListRes(result.getRecords()), result.getTotal());
    }

    @Operation(summary = "根据岗位编号获取详细信息")
    @GetMapping(value = "/{postId}")
    public R<SystemPostGetInfoRes> getInfo(@PathVariable Long postId) {
        return R.ok(PostResConverter.toResToSystemPostGetInfoRes(postDetailQueryExe.execute(postId)));
    }

    @Operation(summary = "新增岗位")
    @RequiresPermissions("system:post:add")
    @Log(title = "岗位管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Boolean> add(@Validated @RequestBody PostAddReq req) {
        return R.ok(postAddCmdExe.execute(PostReqConverter.toCmd(req)));
    }

    @Operation(summary = "修改岗位")
    @RequiresPermissions("system:post:edit")
    @Log(title = "岗位管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Boolean> edit(@Validated @RequestBody PostUpdateReq req) {
        return R.ok(postUpdateCmdExe.execute(PostReqConverter.toCmd(req)));
    }

    @Operation(summary = "删除岗位")
    @RequiresPermissions("system:post:remove")
    @Log(title = "岗位管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{postIds}")
    public R<Boolean> remove(@PathVariable List<Long> postIds) {
        return R.ok(postDeleteCmdExe.execute(postIds));
    }
}
