package com.lemnt.mall.client;
import com.lemnt.mall.client.bo.ProductCommentSaveBo;
/** 私有购买评价保存契约，由交易核验订单后调用，避免商城反向查锁定订单。 */
public interface ProductCommentClient {
    /** 原完整内容幂等保存，返回稳定评价编号。 */
    Long saveVerified(ProductCommentSaveBo value);
    /** 系统到期评价，固定空正文五分，不作为HTTP能力。 */
    Long saveSystem(ProductCommentSaveBo value);
}
