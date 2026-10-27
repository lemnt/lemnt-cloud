package com.lemnt.system.adapter.web.file;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresLogin;
import com.lemnt.system.adapter.web.file.converter.FileReqConverter;
import com.lemnt.system.adapter.web.file.converter.FileResConverter;
import com.lemnt.system.adapter.web.file.req.FileUploadReq;
import com.lemnt.system.adapter.web.file.res.FileUploadRes;
import com.lemnt.system.application.file.executor.FileUploadCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.io.IOException;

/** 已登录用户共用的文件上传入口。 */
@Tag(name = "统一文件上传")
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {
    private final FileUploadCmdExe fileUploadCmdExe;

    @Operation(summary = "上传文件到 OSS")
    @RequiresLogin
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<FileUploadRes> upload(@Validated @ModelAttribute FileUploadReq req) throws IOException {
        var cmd = FileReqConverter.toCmd(req);

        // 存储成功或失败都关闭上传流，不将 HTTP 文件类型传入应用层。
        try (var content = cmd.content()) {
            return R.ok(FileResConverter.toRes(fileUploadCmdExe.execute(cmd)));
        }
    }
}
