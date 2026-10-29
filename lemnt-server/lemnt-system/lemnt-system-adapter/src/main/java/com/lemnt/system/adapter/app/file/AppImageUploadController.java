package com.lemnt.system.adapter.app.file;
import com.lemnt.common.core.domain.R;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.security.annotation.RequiresLogin;
import com.lemnt.system.adapter.app.file.req.ImageUploadReq;
import com.lemnt.system.adapter.app.file.res.ImageUploadRes;
import com.lemnt.system.application.file.cmd.FileUploadCmd;
import com.lemnt.system.application.file.executor.FileUploadCmdExe;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
/** 会员图片上传复用统一文件存储，身份由网关会员域校验。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/files/app")
public class AppImageUploadController {
    private final FileUploadCmdExe uploads;
    /** 校验图片类型、大小及文件头，不向存储传入未验证的任意文件。 */
    @PostMapping(value="/upload", consumes="multipart/form-data")
    @RequiresLogin
    public R<ImageUploadRes> upload(@jakarta.validation.Valid @ModelAttribute ImageUploadReq req) throws IOException {
        var file=req.file();
        if (file == null || file.isEmpty() || file.getSize()>10*1024*1024) throw new ServiceException("图片不能为空且不能超过10MiB",400);
        String type=file.getContentType();
        try (var content=file.getInputStream()) {
            byte[] h=content.readNBytes(12);
            boolean valid = "image/jpeg".equals(type) && h.length>=3 && (h[0]&255)==255 && (h[1]&255)==216 && (h[2]&255)==255
                || "image/png".equals(type) && h.length>=8 && java.util.Arrays.equals(java.util.Arrays.copyOf(h,8),new byte[]{(byte)137,80,78,71,13,10,26,10})
                || "image/webp".equals(type) && h.length>=12 && new String(h,0,4,java.nio.charset.StandardCharsets.US_ASCII).equals("RIFF") && new String(h,8,4,java.nio.charset.StandardCharsets.US_ASCII).equals("WEBP");
            if (!valid) throw new ServiceException("只支持JPEG、PNG、WebP图片",400);
        }
        try (var content=file.getInputStream()) {
            var value=uploads.execute(new FileUploadCmd(content,"image." + ("image/jpeg".equals(type) ? "jpg" : "image/png".equals(type) ? "png" : "webp"),type,file.getSize()));
            return R.ok(new ImageUploadRes(value.url()));
        }
    }
}
