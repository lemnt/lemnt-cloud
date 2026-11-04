package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.dto.KefuMessageDTO;
import com.lemnt.promotion.application.promotion.qry.AppKefuMessageQry;
import com.lemnt.promotion.application.promotion.service.KefuChatService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 小程序本人客服消息查询执行器。
 */
@Component
@RequiredArgsConstructor
public class AppKefuMessageListQueryExe {
    private final KefuChatService service;

    public List<KefuMessageDTO> execute(AppKefuMessageQry qry) {
        return service.appMessages(qry);
    }
}
