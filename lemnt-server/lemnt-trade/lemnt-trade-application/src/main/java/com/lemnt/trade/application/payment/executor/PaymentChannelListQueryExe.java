package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.application.payment.dto.PaymentChannelDTO;
import com.lemnt.trade.domain.payment.model.PaymentChannel;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/** 支持的支付渠道展示查询。 */
@Component
public class PaymentChannelListQueryExe {
    /** 与支付单创建复用同一渠道定义。 */
    public List<PaymentChannelDTO> execute() {
        return Arrays.stream(PaymentChannel.values())
                .map(channel -> new PaymentChannelDTO(channel.name(), channel.displayName())).toList();
    }
}
