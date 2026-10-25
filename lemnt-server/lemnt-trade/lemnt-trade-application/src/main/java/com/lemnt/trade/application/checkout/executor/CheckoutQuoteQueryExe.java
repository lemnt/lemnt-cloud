package com.lemnt.trade.application.checkout.executor;

import com.lemnt.trade.application.checkout.cmd.*;
import com.lemnt.trade.application.checkout.dto.*;
import com.lemnt.trade.application.checkout.converter.CheckoutDTOConverter;
import com.lemnt.trade.domain.checkout.model.*;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CheckoutQuoteQueryExe {
    private final com.lemnt.trade.application.checkout.service.OrderPlacementService placement;

    public CheckoutQuoteDTO execute(
            com.lemnt.trade.application.checkout.qry.CheckoutQuoteQry query) {
        return CheckoutDTOConverter.quote(placement.quote(query));
    }
}
