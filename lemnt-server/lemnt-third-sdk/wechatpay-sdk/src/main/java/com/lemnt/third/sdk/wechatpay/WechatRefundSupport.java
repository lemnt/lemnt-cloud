package com.lemnt.third.sdk.wechatpay;

import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.Amount;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.QueryByOutRefundNoRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import com.wechat.pay.java.service.refund.model.Status;

import java.util.Set;

/**
 * 微信退款：本地退款单号即渠道幂等键，状态归并为成功、处理中、失败。
 */
final class WechatRefundSupport {
    /**
     * 微信对不存在的退款单返回的业务错误码，表示渠道尚未受理。
     */
    private static final Set<String> NOT_FOUND_CODES =
        Set.of("RESOURCE_NOT_EXISTS", "ORDER_NOT_EXIST", "ORDERNOTEXIST");
    private static final int MAX_REASON_LENGTH = 80;

    private WechatRefundSupport() {
    }

    static WechatRefundResult create(RefundService service, WechatRefundCommand command, String notifyUrl) {
        CreateRequest request = new CreateRequest();
        request.setOutTradeNo(command.outTradeNo());
        request.setOutRefundNo(command.outRefundNo());
        request.setReason(reason(command.reason()));
        if (notifyUrl != null && !notifyUrl.isBlank()) {
            request.setNotifyUrl(notifyUrl);
        }
        AmountReq amount = new AmountReq();
        amount.setCurrency(WechatPrepaySupport.CURRENCY);
        amount.setRefund(WechatPrepaySupport.fenLong(command.refundAmount(), "退款金额"));
        amount.setTotal(WechatPrepaySupport.fenLong(command.totalAmount(), "原支付金额"));
        request.setAmount(amount);
        return toResult(service.create(request));
    }

    /**
     * 查询原退款单，渠道未受理时返回 null，由调用方决定是否提交新退款。
     */
    static WechatRefundResult query(RefundService service, String outRefundNo) {
        WechatPrepaySupport.requireText(outRefundNo, 64, "商户退款单号");
        QueryByOutRefundNoRequest request = new QueryByOutRefundNoRequest();
        request.setOutRefundNo(outRefundNo);
        try {
            return toResult(service.queryByOutRefundNo(request));
        } catch (ServiceException exception) {
            if (NOT_FOUND_CODES.contains(exception.getErrorCode())) {
                return null;
            }
            throw exception;
        }
    }

    private static WechatRefundResult toResult(Refund value) {
        if (value == null) {
            throw new IllegalStateException("微信退款未返回结果");
        }
        return result(value.getOutRefundNo(), value.getRefundId(), value.getOutTradeNo(),
            value.getAmount(), value.getStatus());
    }

    static WechatRefundResult notification(RefundNotification value) {
        if (value == null || value.getRefundStatus() == null) {
            throw new IllegalStateException("微信退款通知缺少状态");
        }
        return result(value.getOutRefundNo(), value.getRefundId(), value.getOutTradeNo(),
            value.getAmount(), value.getRefundStatus());
    }

    private static WechatRefundResult result(String outRefundNo, String refundId, String outTradeNo,
            Amount amount, Status status) {
        if (amount == null || amount.getRefund() == null || amount.getRefund() <= 0
                || (amount.getCurrency() != null && !WechatPrepaySupport.CURRENCY.equals(amount.getCurrency()))) {
            throw new IllegalStateException("微信退款金额或币种不合法");
        }
        return new WechatRefundResult(outRefundNo, refundId, outTradeNo,
            WechatPrepaySupport.yuan(amount.getRefund()), status(status));
    }

    private static WechatRefundStatus status(Status status) {
        if (status == null) {
            return WechatRefundStatus.PROCESSING;
        }
        return switch (status) {
            case SUCCESS -> WechatRefundStatus.SUCCESS;
            case PROCESSING -> WechatRefundStatus.PROCESSING;
            case CLOSED, ABNORMAL -> WechatRefundStatus.FAILED;
        };
    }

    private static String reason(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.length() <= MAX_REASON_LENGTH
            ? value
            : value.substring(0, MAX_REASON_LENGTH);
    }
}
