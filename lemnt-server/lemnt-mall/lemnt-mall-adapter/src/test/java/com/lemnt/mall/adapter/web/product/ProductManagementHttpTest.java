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
        when(query.execute(any())).thenReturn(PageResult.of(List.of(new ProductSpecDTO(1L, "颜色", null, null)), 1));
        var controller = new ProductSpecController(mock(ProductSpecCreateCmdExe.class), mock(ProductSpecUpdateCmdExe.class),
                mock(ProductSpecDeleteCmdExe.class), mock(ProductSpecDetailQueryExe.class), query,
                mock(ProductSpecSimpleListQueryExe.class));
        var mvc = MockMvcBuilders.standaloneSetup(controller).setMessageConverters(converter()).build();
        mvc.perform(get("/mall/specs").param("name", "颜色"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows[0].name").value("颜色"))
                .andExpect(jsonPath("$.total").value(1));
        verify(query).execute(new ProductSpecPageQry("颜色", 1, 20, null, null));
        mvc.perform(get("/mall/specs").param("pageSize", "101")).andExpect(status().isBadRequest());
        verifyNoMoreInteractions(query);
    }

    /** 时间查询绑定到独立分页契约，创建时间输出来自持久化记录。 */
    @Test
    void specPageBindsTimeRangeAndReturnsPersistedCreationTime() throws Exception {
        var query = mock(ProductSpecPageQueryExe.class);
        var time = java.time.LocalDateTime.of(2026, 10, 15, 12, 0);
        when(query.execute(any())).thenReturn(PageResult.of(
                List.of(new ProductSpecDTO(1L, "颜色", null, time)), 1));
        var controller = new ProductSpecController(mock(ProductSpecCreateCmdExe.class), mock(ProductSpecUpdateCmdExe.class),
                mock(ProductSpecDeleteCmdExe.class), mock(ProductSpecDetailQueryExe.class), query,
                mock(ProductSpecSimpleListQueryExe.class));
        var mvc = MockMvcBuilders.standaloneSetup(controller).setMessageConverters(converter()).build();
        mvc.perform(get("/mall/specs").param("createTimeStart", "2026-10-15T12:00:00")
                        .param("createTimeEnd", "2026-10-15T12:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows[0].createTime").value("2026-10-15 12:00:00"));
        verify(query).execute(new ProductSpecPageQry(null, 1, 20, time, time));
        mvc.perform(get("/mall/specs").param("createTimeStart", "not-a-date"))
                .andExpect(status().isBadRequest());
        verifyNoMoreInteractions(query);
    }

    @Test
    void propertyValueCannotChangeParentThroughUpdatePayloadAndMissingParentRejects() throws Exception {
        var create = mock(ProductSpecValueCreateCmdExe.class);
        var update = mock(ProductSpecValueUpdateCmdExe.class);
        when(update.execute(any())).thenReturn(new ProductSpecValueDTO(3L, 1L, "蓝", null, null));
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


    /** 状态数量使用与分页相同的名称、分类、品牌条件，保持统一成功响应。 */
    @Test
    void spuCountsBindSharedBusinessFiltersWithoutPageOrStatusFilters() throws Exception {
        var count = mock(ProductSpuCountQueryExe.class);
        when(count.execute(any())).thenReturn(new com.lemnt.mall.application.product.dto.ProductSpuStatusCountDTO(3, 2, 1, 1, 2));
        var controller = new ProductSpuManagementController(mock(ProductSpuRecycleCmdExe.class),
                mock(ProductSpuRestoreCmdExe.class), mock(ProductSpuDeleteCmdExe.class), count,
                mock(ProductSpuSimpleListQueryExe.class), mock(ProductSpuBatchDetailQueryExe.class));
        var mvc = MockMvcBuilders.standaloneSetup(controller).setMessageConverters(converter()).build();
        mvc.perform(get("/mall/spus/count").param("name", "商品").param("categoryId", "1")
                        .param("brandId", "2").param("status", "RECYCLED").param("pageNum", "9"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.onShelf").value(3))
                .andExpect(jsonPath("$.data.offShelf").value(2))
                .andExpect(jsonPath("$.data.recycled").value(1))
                .andExpect(jsonPath("$.data.soldOut").value(1))
                .andExpect(jsonPath("$.data.lowStock").value(2));
        verify(count).execute(new com.lemnt.mall.application.product.qry.ProductSpuCountQry("商品", 1L, 2L));
        mvc.perform(get("/mall/spus/count").param("categoryId", "0"))
                .andExpect(status().isBadRequest());
        verifyNoMoreInteractions(count);
    }

    /** 库存分组是查询条件，响应中的商品状态仍为真实上下架状态。 */
    @Test
    void stockViewBindsIndependentlyFromLifecycleStatus() throws Exception {
        var query = mock(ProductSpuPageQueryExe.class);
        when(query.execute(any())).thenReturn(new com.lemnt.mall.application.product.dto.ProductSpuPageDTO(List.of(), 0));
        var mvc = MockMvcBuilders.standaloneSetup(new ProductSpuController(query, mock(ProductSpuDetailQueryExe.class),
                mock(ProductSpuCreateCmdExe.class), mock(ProductSpuUpdateCmdExe.class), mock(ProductSpuShelfCmdExe.class)))
                .setMessageConverters(converter()).build();
        mvc.perform(get("/mall/spus").param("stockStatus", "LOW_STOCK").param("categoryId", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(0));
        verify(query).execute(new com.lemnt.mall.application.product.qry.ProductSpuPageQry(
                null, null, 1, 10, 1L, null, "LOW_STOCK"));
    }

    /** 页面使用既有回收、恢复、删除路径，不将false伪装为成功记录。 */
    @Test
    void spuLifecycleCommandsUseIndependentExistingRoutesAndBooleanResults() throws Exception {
        var recycle = mock(ProductSpuRecycleCmdExe.class);
        var restore = mock(ProductSpuRestoreCmdExe.class);
        var delete = mock(ProductSpuDeleteCmdExe.class);
        when(recycle.execute(9L)).thenReturn(true);
        when(restore.execute(9L)).thenReturn(true);
        when(delete.execute(9L)).thenReturn(false);
        var controller = new ProductSpuManagementController(recycle, restore, delete,
                mock(ProductSpuCountQueryExe.class), mock(ProductSpuSimpleListQueryExe.class),
                mock(ProductSpuBatchDetailQueryExe.class));
        var mvc = MockMvcBuilders.standaloneSetup(controller).setMessageConverters(converter()).build();
        mvc.perform(post("/mall/spus/9/recycle")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
        mvc.perform(post("/mall/spus/9/restore")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
        mvc.perform(delete("/mall/spus/9")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));
        verify(recycle).execute(9L);
        verify(restore).execute(9L);
        verify(delete).execute(9L);
    }

    @Test
    void consumerCannotChooseReviewerIdentityAndEmptyContentIsRejectedBeforeCreate() throws Exception {
        var create = mock(CommentCreateCmdExe.class);
        when(create.execute(any())).thenReturn(new ProductCommentDTO(1L, 3L, 9L, "TR1", 7L, 5, "真实评价",
                true, null, null, null, null));
        var mvc = MockMvcBuilders.standaloneSetup(new AppCommentController(create, mock(com.lemnt.mall.application.product.executor.CommentPublicPageQueryExe.class)))
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
