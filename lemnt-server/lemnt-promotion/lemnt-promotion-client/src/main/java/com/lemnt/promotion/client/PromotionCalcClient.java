package com.lemnt.promotion.client;
import com.lemnt.promotion.client.bo.ActivityPriceBo;
import com.lemnt.promotion.client.bo.PromotionCalculateBo;
import com.lemnt.promotion.client.dto.ActivityQuoteDTO;
import com.lemnt.promotion.client.dto.PromotionQuoteDTO;
/** 营销只生成新报价，已提交订单使用自身冻结结果，重试不重新定价。 */
public interface PromotionCalcClient {
    PromotionQuoteDTO calculate(PromotionCalculateBo input);
    ActivityQuoteDTO priceActivity(ActivityPriceBo input);
}
