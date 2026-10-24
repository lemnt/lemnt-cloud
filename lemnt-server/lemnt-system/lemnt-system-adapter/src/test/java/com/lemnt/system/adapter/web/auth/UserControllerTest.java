package com.lemnt.system.adapter.web.auth;

import com.lemnt.common.adapter.config.GlobalRequestBindingAdvice;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.application.auth.executor.UserPageQueryExe;
import com.lemnt.system.application.auth.qry.UserPageQry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserPageQueryExe userPageQueryExe;

    @InjectMocks
    private UserController controller;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalRequestBindingAdvice())
                .build();
        when(userPageQueryExe.execute(any())).thenReturn(PageResult.of(List.of(), 0L));
    }

    @Test
    void shouldNotUseLoginUsernameHeaderAsListFilter() throws Exception {
        mvc.perform(get("/users")
                        .header("username", "admin")
                        .param("pageNum", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk());

        ArgumentCaptor<UserPageQry> query = ArgumentCaptor.forClass(UserPageQry.class);
        verify(userPageQueryExe).execute(query.capture());
        assertNull(query.getValue().userName());
        assertEquals(1, query.getValue().pageNum());
        assertEquals(20, query.getValue().pageSize());
    }

    @Test
    void shouldPreserveExplicitUsernameAndDepartmentFilters() throws Exception {
        mvc.perform(get("/users")
                        .header("username", "admin")
                        .param("userName", "ry")
                        .param("deptId", "105"))
                .andExpect(status().isOk());

        ArgumentCaptor<UserPageQry> query = ArgumentCaptor.forClass(UserPageQry.class);
        verify(userPageQueryExe).execute(query.capture());
        assertEquals("ry", query.getValue().userName());
        assertEquals(105L, query.getValue().deptId());
        assertEquals(1, query.getValue().pageNum());
        assertEquals(10, query.getValue().pageSize());
    }
}
