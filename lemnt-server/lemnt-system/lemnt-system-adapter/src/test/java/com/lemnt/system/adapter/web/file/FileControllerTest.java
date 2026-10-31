package com.lemnt.system.adapter.web.file;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.security.annotation.RequiresLogin;
import com.lemnt.common.security.handler.GlobalExceptionHandler;
import com.lemnt.system.adapter.web.file.req.FileUploadReq;
import com.lemnt.system.application.file.executor.FileUploadCmdExe;
import com.lemnt.system.domain.file.model.UploadFile;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FileControllerTest {
    @Test
    void shouldBindMultipartFileAndReturnUrl() throws Exception {
        AtomicReference<UploadFile> captured = new AtomicReference<>();

        var controller = new FileController(new FileUploadCmdExe(file -> {
                captured.set(file);

                return "https://oss.example.test/assets/image.png";

                }));

        var mvc = MockMvcBuilders.standaloneSetup(controller).build();

        mvc.perform(multipart("/files/upload")
                .file(new MockMultipartFile("file", "image.png", "image/png", new byte[]{
                1, 2
                })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.url").value("https://oss.example.test/assets/image.png"));

        assertEquals("image.png", captured.get().fileName());

        assertEquals("image/png", captured.get().contentType());

        assertEquals(2, captured.get().size());

    }

    @Test
    void shouldRejectMissingAndEmptyFileBeforeStorage() throws Exception {
        var controller = new FileController(new FileUploadCmdExe(file -> {
                throw new AssertionError("空文件不应调用存储");

                }));

        var mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler()).build();

        mvc.perform(multipart("/files/upload"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.detail").value("上传文件不能为空"))
                .andExpect(jsonPath("$.errors[0].pointer").value("/file"));

        mvc.perform(multipart("/files/upload")
                .file(new MockMultipartFile("file", "empty.png", "image/png", new byte[0])))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"))
                .andExpect(jsonPath("$.detail").value("上传文件不能为空"));

    }

    @Test
    void shouldCloseStreamOnSuccessAndFailure() throws Exception {
        for (boolean fail : new boolean[]{
                false, true
                }) {
            AtomicBoolean closed = new AtomicBoolean();

            var file = new MockMultipartFile("file", "image.png", "image/png", new byte[]{
                    1
                    }) {
                @Override
                public InputStream getInputStream() {
                    return new ByteArrayInputStream(new byte[]{
                            1
                            }) {
                        @Override
                        public void close() {
                            closed.set(true);

                        }
                    };

                }
            };

            var controller = new FileController(new FileUploadCmdExe(upload -> {
                    assertFalse(closed.get());

                    if (fail) throw new ServiceException("测试存储失败");

                    return "https://oss.example.test/image.png";

                    }));

            if (fail) {
                assertThrows(ServiceException.class, () -> controller.upload(new FileUploadReq(file)));

            } else {
                controller.upload(new FileUploadReq(file));

            }
            assertTrue(closed.get());

        }
    }

    @Test
    void shouldRequireLogin() throws Exception {
        assertNotNull(FileController.class.getMethod("upload", FileUploadReq.class)
                .getAnnotation(RequiresLogin.class));

    }
}
