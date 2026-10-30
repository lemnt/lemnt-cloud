package com.lemnt.member.infrastructure.growth.gateway;
import com.lemnt.member.domain.growth.gateway.MemberExperienceRecordGateway;
import com.lemnt.member.domain.growth.model.*;
import com.lemnt.member.infrastructure.growth.dataobject.ExperienceRecordDO;
import com.lemnt.member.infrastructure.growth.converter.ExperienceRecordConverter;
import com.lemnt.member.infrastructure.growth.mapper.MemberExperienceRecordMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
/** 只读写经验表，不跨存储查找同编号记录。 */
@Component @RequiredArgsConstructor
public class MemberExperienceRecordGatewayImpl implements MemberExperienceRecordGateway {
    private final MemberExperienceRecordMapper mapper;
    @Override public AssetRecord request(Long memberId,String code,String bizId) {
        return ExperienceRecordConverter.toModel(mapper.selectOne(Wrappers.lambdaQuery(ExperienceRecordDO.class)
            .eq(ExperienceRecordDO::getUserId,memberId).eq(ExperienceRecordDO::getBizCode,code).eq(ExperienceRecordDO::getBizId,bizId)));
    }
    @Override public void save(AssetRecord record) {
        if (!"EXPERIENCE".equals(record.assetType())) throw new com.lemnt.common.core.exception.ServiceException("账本资产类型不匹配",400);
        mapper.insert(ExperienceRecordConverter.toDO(record)); }
    @Override public List<AssetRecord> page(AssetRecordPageFilter filter) {return mapper.page(filter).stream().map(ExperienceRecordConverter::toModel).toList();}
    @Override public long count(AssetRecordPageFilter filter) {return mapper.count(filter);}
    @Override public AssetRecord detail(Long id) {return ExperienceRecordConverter.toModel(mapper.selectById(id));}
    @Override public long returned(Long memberId,String code,String orderNo) {
        return mapper.selectList(Wrappers.lambdaQuery(ExperienceRecordDO.class).eq(ExperienceRecordDO::getUserId,memberId)
            .eq(ExperienceRecordDO::getBizCode,code).likeRight(ExperienceRecordDO::getBizId,orderNo+":"))
            .stream().mapToLong(ExperienceRecordDO::getExperience).sum();
    }
}
