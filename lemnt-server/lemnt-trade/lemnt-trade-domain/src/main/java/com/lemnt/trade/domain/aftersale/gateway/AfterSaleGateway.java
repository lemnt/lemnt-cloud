package com.lemnt.trade.domain.aftersale.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.domain.aftersale.model.AfterSale;
import com.lemnt.trade.domain.aftersale.model.AfterSaleFilter;

import java.time.LocalDateTime;
import java.util.List;

/** 售后表单表存储契约。 */
public interface AfterSaleGateway {
    /** 后台售后分页。 */
    PageResult<AfterSale> afterSalePage(AfterSaleFilter filter);
    /** 按唯一业务单号读取售后。 */
    AfterSale afterSale(String no);
    /** 锁定售后，调用方先持有订单锁。 */
    AfterSale lockAfterSale(String no);
    /** 查询订单全部售后事实，用于额度校验。 */
    List<AfterSale> afterSales(String orderNo);
    /** 创建售后，由数据库分配数值主键。 */
    void saveAfterSale(AfterSale value);
    /** 条件更新售后审批或退货状态。 */
    boolean updateAfterSale(AfterSale value, String expectedStatus);
    /** 创建支付退款时占用售后，防止取消或拒绝。 */
    boolean startPaymentRefund(String no);
    /** 仅真实渠道成功才能完成售后并记录退款时间。 */
    boolean completePaymentRefund(String no, LocalDateTime time);
    /** 是否有影响发货和收货的处理中售后。 */
    boolean activeRefund(String orderNo);
    /** 幂等请求定位完整售后。 */
    AfterSale findRequest(String orderNo, String requestNo);
}
