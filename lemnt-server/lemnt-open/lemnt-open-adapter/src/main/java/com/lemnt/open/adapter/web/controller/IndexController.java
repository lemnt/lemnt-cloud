package com.lemnt.open.adapter.web.controller;

import com.lemnt.common.core.domain.R;
import com.lemnt.open.adapter.web.converter.IndexResConverter;
import com.lemnt.open.adapter.web.res.IndexInfoRes;
import com.lemnt.open.application.index.executor.IndexQryExe;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "开放平台")
@RestController
@RequestMapping("/open")
@RequiredArgsConstructor
public class IndexController {

    private final IndexQryExe indexQryExe;

    @Operation(summary = "测试接口")
    @GetMapping("/test")
    public R<IndexInfoRes> test() {
        return R.ok(IndexResConverter.toRes(indexQryExe.execute()));
    }
}
