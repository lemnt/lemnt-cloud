package com.lemnt.system.adapter.app.file;
import com.lemnt.system.application.file.executor.FileUploadCmdExe;
import com.lemnt.common.security.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
/** 上传边界与真实执行器映射，不替代部署认证和OSS验收。 */
class AppImageUploadControllerTest {
    @Test
    void checksSignatureAndPassesUnconsumedStreamToExistingStorage() throws Exception {
        byte[] png={(byte)137,80,78,71,13,10,26,10};
        var mvc=MockMvcBuilders.standaloneSetup(new AppImageUploadController(new FileUploadCmdExe(file->{
            try { assertArrayEquals(png,file.content().readAllBytes()); } catch(java.io.IOException e){throw new RuntimeException(e);}
            return "https://example.test/image.png";
        }))).setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(multipart("/files/app/upload").file(new MockMultipartFile("file","a.png","image/png",png)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.url").value("https://example.test/image.png"));
    }
    @Test
    void rejectsFakeImagesMissingAndOversizedFilesBeforeStorage() throws Exception {
        var mvc=MockMvcBuilders.standaloneSetup(new AppImageUploadController(new FileUploadCmdExe(file->{
            fail("非法文件不应进入存储");return null;
        }))).setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(multipart("/files/app/upload").file(new MockMultipartFile("file","a.png","image/png",new byte[]{1,2})))
            .andExpect(status().isBadRequest());
        mvc.perform(multipart("/files/app/upload").file(new MockMultipartFile("file","a.jpg","image/jpeg",new byte[10*1024*1024+1])))
            .andExpect(status().isBadRequest());
        mvc.perform(multipart("/files/app/upload")).andExpect(status().is(422));
    }
}
