package com.lemnt.open.adapter.web.index;

import com.lemnt.common.core.domain.R;
import com.lemnt.open.adapter.web.index.converter.IndexResConverter;
import com.lemnt.open.adapter.web.index.res.IndexInfoRes;
import com.lemnt.open.application.index.executor.IndexDetailQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@Tag(name = "开放平台")
@RestController
@RequestMapping("/open")
@RequiredArgsConstructor
public class IndexController {
    private final IndexDetailQueryExe indexDetailQueryExe;

    @Operation(summary = "测试接口")
    @GetMapping("/test")
    public R<IndexInfoRes> test() {
        return R.ok(IndexResConverter.toRes(indexDetailQueryExe.execute()));
    }
}
