package com.lemnt.system.adapter.web.auth;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.adapter.web.auth.converter.MenuResConverter;
import com.lemnt.system.adapter.web.auth.converter.UserResConverter;
import com.lemnt.system.adapter.web.config.DictDataController;
import com.lemnt.system.application.auth.dto.MetaDTO;
import com.lemnt.system.application.auth.dto.RoleDTO;
import com.lemnt.system.application.auth.dto.RouterDTO;
import com.lemnt.system.application.auth.dto.UserDTO;
import com.lemnt.system.application.config.executor.*;
import com.lemnt.system.application.config.qry.DictDataPageQry;
import com.lemnt.system.application.org.dto.DeptDTO;
import com.lemnt.system.application.org.executor.DeptTreeQueryExe;
import com.lemnt.system.application.org.qry.DeptListQry;
import com.lemnt.system.domain.auth.model.TreeSelect;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 重构HTTP边界时保留嵌套JSON、空值、查询字段和原有R分页形状。 */
class BoundaryRefactorContractTest {
    final JsonMapper json =
            JsonMapper.builder()
                    .changeDefaultPropertyInclusion(
                            inclusion -> inclusion.withValueInclusion(JsonInclude.Include.NON_NULL))
                    .build();

    @Test
    void nestedUserRolesAndDepartmentsKeepTheirOriginalJson() {
        var user = new UserDTO();
        var dept = new DeptDTO();
        dept.setDeptId(9007199254740993L);
        dept.setDeptName("研发");
        var role = new RoleDTO();
        role.setRoleId(10L);
        role.setRoleName("开发");
        user.setDept(dept);
        for (boolean unknownChildren : new boolean[] {true, false}) {
            dept.setChildren(unknownChildren ? null : List.of());
            for (boolean unknownRoles : new boolean[] {true, false}) {
                user.setRoles(unknownRoles ? null : List.of(role));
                var response = UserResConverter.toRes(user);
                assertEquals(json.valueToTree(user.getDept()), json.valueToTree(response.dept()));
                assertEquals(json.valueToTree(user.getRoles()), json.valueToTree(response.roles()));
            }
        }
    }

    @Test
    void routerMetadataStillHasTheSameFieldsAndBooleanValue() {
        var router = new RouterDTO();
        router.setName("菜单");
        var meta = new MetaDTO("", "menu", false, "https://example.com");
        router.setMeta(meta);
        var response = MenuResConverter.toRouterInfoRes(router);
        assertEquals(json.valueToTree(meta), json.valueToTree(response.meta()));
        assertFalse(json.valueToTree(response.meta()).get("noCache").asBoolean());
    }

    @Test
    void dictionaryHttpParametersConvertToApplicationQueryAndValidatePaging() throws Exception {
        var query = mock(DictDataPageQueryExe.class);
        when(query.execute(any())).thenReturn(PageResult.of(List.of(), 0));
        var controller =
                new DictDataController(
                        query,
                        mock(DictDataDetailQueryExe.class),
                        mock(DictDataAddCmdExe.class),
                        mock(DictDataUpdateCmdExe.class),
                        mock(DictDataDeleteCmdExe.class),
                        mock(DictDataListQueryExe.class));
        var mvc = MockMvcBuilders.standaloneSetup(controller).build();
        var response =
                mvc.perform(get("/dictionary-data").param("dictType", "sample"))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse();
        var argument = ArgumentCaptor.forClass(DictDataPageQry.class);
        verify(query).execute(argument.capture());
        assertEquals("sample", argument.getValue().dictType());
        assertEquals(1, argument.getValue().pageNum());
        assertEquals(10, argument.getValue().pageSize());
        assertTrue(json.readTree(response.getContentAsString()).get("rows").isArray());
        mvc.perform(get("/dictionary-data").param("pageNum", "0"))
                .andExpect(status().isBadRequest());
        verifyNoMoreInteractions(query);
    }

    @Test
    void userDepartmentTreePreservesEmptyLabelFalseAndOmittedEmptyChildren() throws Exception {
        var query = mock(DeptTreeQueryExe.class);
        var tree = new TreeSelect();
        tree.setId(7L);
        tree.setLabel("");
        tree.setChildren(List.of());
        when(query.execute(any())).thenReturn(List.of(tree));
        var mvc = MockMvcBuilders.standaloneSetup(new UserDeptController(query)).build();
        var response =
                mvc.perform(get("/users/dept-tree").param("deptName", "研发").param("status", "1"))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse();
        var node = json.readTree(response.getContentAsString()).get("data").get(0);
        assertEquals("", node.get("label").asString());
        assertFalse(node.get("disabled").asBoolean());
        assertNull(node.get("children"));
        var argument = ArgumentCaptor.forClass(DeptListQry.class);
        verify(query).execute(argument.capture());
        assertEquals("研发", argument.getValue().deptName());
        assertEquals("1", argument.getValue().status());
    }
}
