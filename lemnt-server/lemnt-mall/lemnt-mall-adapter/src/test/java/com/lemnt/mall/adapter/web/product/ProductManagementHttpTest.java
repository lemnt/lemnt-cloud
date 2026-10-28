package com.lemnt.mall.adapter.web.product;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.security.constant.SecurityConstants;
import com.lemnt.common.security.interceptor.HeaderInterceptor;
import com.lemnt.common.start.config.JacksonConfig;
import com.lemnt.mall.adapter.app.product.AppCommentController;
import com.lemnt.mall.application.product.cmd.CommentCreateCmd;
import com.lemnt.mall.application.product.cmd.ProductSpecValueUpdateCmd;
import com.lemnt.mall.application.product.dto.ProductCommentDTO;
import com.lemnt.mall.application.product.dto.ProductSpecDTO;
import com.lemnt.mall.application.product.dto.ProductSpecValueDTO;
import com.lemnt.mall.application.product.executor.*;
import com.lemnt.mall.application.product.executor.CommentCreateCmdExe;
import com.lemnt.mall.application.product.executor.ProductSpecCreateCmdExe;
import com.lemnt.mall.application.product.executor.ProductSpecDeleteCmdExe;
import com.lemnt.mall.application.product.executor.ProductSpecDetailQueryExe;
import com.lemnt.mall.application.product.executor.ProductSpecPageQueryExe;
import com.lemnt.mall.application.product.executor.ProductSpecSimpleListQueryExe;
import com.lemnt.mall.application.product.executor.ProductSpecUpdateCmdExe;
import com.lemnt.mall.application.product.executor.ProductSpecValueCreateCmdExe;
import com.lemnt.mall.application.product.executor.ProductSpecValueDeleteCmdExe;
import com.lemnt.mall.application.product.executor.ProductSpecValueDetailQueryExe;
import com.lemnt.mall.application.product.executor.ProductSpecValuePageQueryExe;
import com.lemnt.mall.application.product.executor.ProductSpecValueSimpleListQueryExe;
import com.lemnt.mall.application.product.executor.ProductSpecValueUpdateCmdExe;
import com.lemnt.mall.application.product.qry.ProductSpecPageQry;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductManagementHttpTest {
    JacksonJsonHttpMessageConverter converter() {
        var builder = JsonMapper.builder();
        new JacksonConfig().customizer().customize(builder);
        return new JacksonJsonHttpMessageConverter(builder.build());
    }

    @Test
    void propertyPagingUsesCommonEnvelopeAndRejectsExcessivePageSizes() throws Exception {
        var query = mock(ProductSpecPageQueryExe.class);
        when(query.execute(any())).thenReturn(PageResult.of(List.of(new ProductSpecDTO(1L, "颜色", null)), 1));
        var controller = new ProductSpecController(mock(ProductSpecCreateCmdExe.class), mock(ProductSpecUpdateCmdExe.class),
                mock(ProductSpecDeleteCmdExe.class), mock(ProductSpecDetailQueryExe.class), query,
                mock(ProductSpecSimpleListQueryExe.class));
        var mvc = MockMvcBuilders.standaloneSetup(controller).setMessageConverters(converter()).build();
        mvc.perform(get("/mall/specs").param("name", "颜色"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows[0].name").value("颜色"))
                .andExpect(jsonPath("$.total").value(1));
        verify(query).execute(new ProductSpecPageQry("颜色", 1, 20));
        mvc.perform(get("/mall/specs").param("pageSize", "101")).andExpect(status().isBadRequest());
        verifyNoMoreInteractions(query);
    }

    @Test
    void propertyValueCannotChangeParentThroughUpdatePayloadAndMissingParentRejects() throws Exception {
        var create = mock(ProductSpecValueCreateCmdExe.class);
        var update = mock(ProductSpecValueUpdateCmdExe.class);
        when(update.execute(any())).thenReturn(new ProductSpecValueDTO(3L, 1L, "蓝", null));
        var controller = new ProductSpecValueController(create, update, mock(ProductSpecValueDeleteCmdExe.class),
                mock(ProductSpecValueDetailQueryExe.class), mock(ProductSpecValuePageQueryExe.class),
                mock(ProductSpecValueSimpleListQueryExe.class));
        var mvc = MockMvcBuilders.standaloneSetup(controller).setMessageConverters(converter()).build();
        mvc.perform(post("/mall/spec-values").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"蓝\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(create);
        mvc.perform(put("/mall/spec-values/3").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"蓝\",\"specId\":999}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.specId").value("1"));
        verify(update).execute(new ProductSpecValueUpdateCmd(3L, "蓝", null));
    }


    @Test
    void consumerCannotChooseReviewerIdentityAndEmptyContentIsRejectedBeforeCreate() throws Exception {
        var create = mock(CommentCreateCmdExe.class);
        when(create.execute(any())).thenReturn(new ProductCommentDTO(1L, 3L, 9L, "TR1", 7L, 5, "真实评价",
                true, null, null, null, null));
        var mvc = MockMvcBuilders.standaloneSetup(new AppCommentController(create))
                .addInterceptors(new HeaderInterceptor()).setMessageConverters(converter()).build();
        mvc.perform(post("/mall/app/comments").header(SecurityConstants.HEADER_USER_ID, "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberId\":999,\"orderNo\":\"TR1\",\"skuId\":9,\"score\":5,\"content\":\"真实评价\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value("1"));
        verify(create).execute(new CommentCreateCmd(7L, "TR1", 9L, 5, "真实评价"));
        mvc.perform(post("/mall/app/comments").header(SecurityConstants.HEADER_USER_ID, "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderNo\":\"TR1\",\"skuId\":9,\"score\":5,\"content\":\" \"}"))
                .andExpect(status().isBadRequest());
        verifyNoMoreInteractions(create);
    }
}
