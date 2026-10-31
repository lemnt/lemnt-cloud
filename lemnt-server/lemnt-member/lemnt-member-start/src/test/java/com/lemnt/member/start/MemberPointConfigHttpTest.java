package com.lemnt.member.start;

import com.lemnt.common.security.handler.GlobalExceptionHandler;
import com.lemnt.member.adapter.web.growth.MemberPointConfigController;
import com.lemnt.member.application.growth.cmd.ConfigSaveCmd;
import com.lemnt.member.application.growth.dto.ConfigDTO;
import com.lemnt.member.application.growth.executor.MemberConfigDetailQueryExe;
import com.lemnt.member.application.growth.executor.MemberConfigSaveCmdExe;
import com.lemnt.member.application.growth.qry.ConfigDetailQry;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MemberPointConfigHttpTest {
    /** 读写使用实际growths路径，积分数上限不转换为金额。 */
    @Test
    void readsAndSavesTheFourConfigurationFields() throws Exception {
        var read = mock(MemberConfigDetailQueryExe.class);
        var save = mock(MemberConfigSaveCmdExe.class);
        when(read.execute(new ConfigDetailQry())).thenReturn(new ConfigDTO(true, 5, 2, 3));
        when(save.execute(new ConfigSaveCmd(false, 0, 0, 0))).thenReturn(true);
        var mvc = MockMvcBuilders.standaloneSetup(new MemberPointConfigController(read, save))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(get("/growths/config")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pointTradeDeductMaxPrice").value(2));
        mvc.perform(put("/growths/config").contentType("application/json")
                .content("{\"pointTradeDeductEnable\":false,\"pointTradeDeductUnitPrice\":0,\"pointTradeDeductMaxPrice\":0,\"pointTradeGivePoint\":0}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").value(true));
        verify(save).execute(new ConfigSaveCmd(false, 0, 0, 0));
    }
}
