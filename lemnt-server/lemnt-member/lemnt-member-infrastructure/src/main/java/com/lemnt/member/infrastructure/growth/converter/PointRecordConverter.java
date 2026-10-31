package com.lemnt.member.infrastructure.growth.converter;
import com.lemnt.member.domain.growth.model.AssetRecord;
import com.lemnt.member.infrastructure.growth.dataobject.PointRecordDO;
/** 积分专属列与既有应用兼容投影转换。 */
public final class PointRecordConverter {
    private PointRecordConverter() {}
    public static AssetRecord toModel(PointRecordDO v) {
        return v==null?null:new AssetRecord(v.getId(),v.getUserId(),"POINT",v.getPoint(),v.getTotalPoint(),
            v.getBizCode(),v.getBizId(),v.getReason(),v.getCreateTime(),v.getNickname(),v.getBizType(),v.getTitle(),v.getDescription());
    }
    public static PointRecordDO toDO(AssetRecord v) {
        var row=new PointRecordDO();row.setId(v.id());row.setUserId(v.memberId());row.setBizId(v.bizNo());
        row.setBizCode(v.bizType());row.setBizType(v.pointBizType());row.setTitle(v.title());row.setDescription(v.description());
        row.setPoint(v.delta());row.setTotalPoint(v.balance());row.setReason(v.reason());row.setCreateTime(v.createTime());row.setUpdateTime(v.createTime());return row;
    }
}
