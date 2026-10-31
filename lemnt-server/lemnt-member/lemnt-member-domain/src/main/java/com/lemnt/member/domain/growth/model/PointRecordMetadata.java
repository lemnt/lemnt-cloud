package com.lemnt.member.domain.growth.model;
/** 源积分枚举快照；只映射语义确定的业务，不猜测未知来源。 */
public record PointRecordMetadata(Integer type,String title,String description) {
    public static PointRecordMetadata from(String assetType,String code,long delta) {
        if (!"POINT".equals(assetType)) return new PointRecordMetadata(null,null,null);
        return switch(code) {
            case "SIGN_IN" -> value(1,"签到","签到获得 {} 积分",delta);
            case "ADMIN" -> value(2,"管理员修改","管理员修改 {} 积分",delta);
            case "TRADE_USE" -> value(11,"订单积分抵扣","下单使用 {} 积分",delta);
            case "TRADE_USE_CANCEL" -> value(12,"订单积分抵扣（整单取消）","订单取消，退还 {} 积分",delta);
            case "TRADE_USE_RETURN" -> value(13,"订单积分抵扣（单个退款）","订单退款，退还 {} 积分",delta);
            case "TRADE_GIVE" -> value(21,"订单积分奖励","下单获得 {} 积分",delta);
            case "TRADE_GIVE_RETURN" -> value(23,"订单积分奖励（单个退款）","订单退款，扣除赠送的 {} 积分",delta);
            default -> new PointRecordMetadata(null,null,null);
        };
    }
    /** 源枚举保留22；没有当前写入来源不等于非法筛选。 */
    public static boolean supported(Integer type) { return type != null && java.util.Set.of(1,2,11,12,13,21,22,23).contains(type); }
    private static PointRecordMetadata value(int type,String title,String template,long delta) {
        return new PointRecordMetadata(type,title,template.replace("{}",Long.toString(delta)));
    }
}
