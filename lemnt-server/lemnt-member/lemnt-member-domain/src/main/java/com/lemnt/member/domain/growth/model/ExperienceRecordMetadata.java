package com.lemnt.member.domain.growth.model;
/** 源经验枚举快照，未知来源不推断。 */
public record ExperienceRecordMetadata(Integer type,String title,String description) {
    /** 源枚举中合法的查询类型，未接入写入路径的类型同样允许查询。 */
    public static boolean supported(Integer type) {
        return type != null && java.util.Set.of(0, 1, 4, 5, 11, 12, 13).contains(type);
    }
    public static ExperienceRecordMetadata from(String assetType,String code,long delta) {
        if(!"EXPERIENCE".equals(assetType))return new ExperienceRecordMetadata(null,null,null);
        return switch(code) {
            case "ADMIN","ADMIN_LEVEL" -> value(0,"管理员调整","管理员调整获得 {} 经验",delta);
            case "SIGN_IN" -> value(4,"签到奖励","签到获得 {} 经验",delta);
            case "TRADE_EXPERIENCE" -> value(11,"下单奖励","下单获得 {} 经验",delta);
            case "TRADE_EXPERIENCE_RETURN" -> value(13,"下单奖励（单个退款）","退款订单获得 {} 经验",delta);
            default -> new ExperienceRecordMetadata(null,null,null);
        };
    }
    private static ExperienceRecordMetadata value(int type,String title,String template,long delta) {
        return new ExperienceRecordMetadata(type,title,template.replace("{}",Long.toString(delta)));
    }
}
