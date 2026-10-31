package com.lemnt.member.infrastructure.growth.gateway;
import com.lemnt.member.domain.growth.gateway.MemberPointRecordGateway;
import com.lemnt.member.domain.growth.model.*;
import com.lemnt.member.infrastructure.growth.dataobject.PointRecordDO;
import com.lemnt.member.infrastructure.growth.converter.PointRecordConverter;
import com.lemnt.member.infrastructure.growth.mapper.MemberPointRecordMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
/** 只读写积分表，不跨存储查找同编号记录。 */
@Component @RequiredArgsConstructor
public class MemberPointRecordGatewayImpl implements MemberPointRecordGateway {
    private final MemberPointRecordMapper mapper;
    @Override public AssetRecord request(Long memberId,String code,String bizId) {
        return PointRecordConverter.toModel(mapper.selectOne(Wrappers.lambdaQuery(PointRecordDO.class)
            .eq(PointRecordDO::getUserId,memberId).eq(PointRecordDO::getBizCode,code).eq(PointRecordDO::getBizId,bizId)));
    }
    @Override public void save(AssetRecord record) {
        if (!"POINT".equals(record.assetType())) throw new com.lemnt.common.core.exception.ServiceException("账本资产类型不匹配",400);
        mapper.insert(PointRecordConverter.toDO(record)); }
    @Override public List<AssetRecord> page(AssetRecordPageFilter filter) {return mapper.page(filter).stream().map(PointRecordConverter::toModel).toList();}
    @Override public long count(AssetRecordPageFilter filter) {return mapper.count(filter);}
    @Override public AssetRecord detail(Long id) {return PointRecordConverter.toModel(mapper.selectById(id));}
    @Override public long returned(Long memberId,String code,String orderNo) {
        return mapper.selectList(Wrappers.lambdaQuery(PointRecordDO.class).eq(PointRecordDO::getUserId,memberId)
            .eq(PointRecordDO::getBizCode,code).likeRight(PointRecordDO::getBizId,orderNo+":"))
            .stream().mapToLong(PointRecordDO::getPoint).sum();
    }
}
