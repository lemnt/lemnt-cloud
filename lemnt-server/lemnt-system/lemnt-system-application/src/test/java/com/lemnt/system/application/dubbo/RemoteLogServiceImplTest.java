package com.lemnt.system.application.dubbo;

import com.lemnt.system.api.domain.bo.LogininforBo;
import com.lemnt.system.domain.log.gateway.LogininforGateway;
import com.lemnt.system.domain.log.gateway.OperLogGateway;
import com.lemnt.system.domain.log.model.Logininfor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RemoteLogServiceImplTest {

    @Test
    void shouldPersistCallerAccessTime() {
        assertSavedAccessTime(LocalDateTime.of(2026, 10, 8, 15, 30, 12));
    }

    @Test
    void shouldNotInventMissingAccessTime() {
        assertSavedAccessTime(null);
    }

    /** 验证调用方时间原样传递，且保存结果不变。 */
    private void assertSavedAccessTime(LocalDateTime time) {
        LogininforGateway gateway = mock(LogininforGateway.class);
        when(gateway.insert(any(Logininfor.class))).thenReturn(true);
        RemoteLogServiceImpl service = new RemoteLogServiceImpl(mock(OperLogGateway.class), gateway);
        LogininforBo bo = new LogininforBo();
        bo.setUserName("admin");
        bo.setAccessTime(time);

        assertThat(service.saveLogininfor(bo)).isTrue();

        ArgumentCaptor<Logininfor> captor = ArgumentCaptor.forClass(Logininfor.class);
        verify(gateway).insert(captor.capture());
        assertThat(captor.getValue().getAccessTime()).isEqualTo(time);
        assertThat(captor.getValue().getUserName()).isEqualTo("admin");
    }
}
