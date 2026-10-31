package com.lemnt.member.infrastructure.growth.converter;
import com.lemnt.member.domain.growth.model.AssetRecord;
import com.lemnt.member.infrastructure.growth.dataobject.ExperienceRecordDO;
/** 经验专属列与既有应用兼容投影转换。 */
public final class ExperienceRecordConverter {
    private ExperienceRecordConverter() {}
    public static AssetRecord toModel(ExperienceRecordDO v) {
        return v==null?null:new AssetRecord(v.getId(),v.getUserId(),"EXPERIENCE",v.getExperience(),v.getTotalExperience(),
            v.getBizCode(),v.getBizId(),v.getReason(),v.getCreateTime(),v.getNickname(),null,v.getTitle(),v.getDescription(),v.getBizType());
    }
    public static ExperienceRecordDO toDO(AssetRecord v) {
        var row=new ExperienceRecordDO();row.setId(v.id());row.setUserId(v.memberId());row.setBizId(v.bizNo());
        row.setBizCode(v.bizType());row.setBizType(v.experienceBizType());row.setTitle(v.title());row.setDescription(v.description());
        row.setExperience(v.delta());row.setTotalExperience(v.balance());row.setReason(v.reason());row.setCreateTime(v.createTime());row.setUpdateTime(v.createTime());return row;
    }
}
