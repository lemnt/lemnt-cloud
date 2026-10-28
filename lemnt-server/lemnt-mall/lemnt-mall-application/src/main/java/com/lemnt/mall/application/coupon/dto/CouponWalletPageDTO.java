package com.lemnt.mall.application.coupon.dto;

import java.io.Serializable;
import java.util.List;

/** 会员券包分页，不复用领券中心模板数据。 */
public record CouponWalletPageDTO(List<CouponWalletDTO> records, long total)
        implements Serializable {
}
