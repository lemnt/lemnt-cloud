package com.lemnt.member.domain.growth.gateway;
import com.lemnt.member.domain.growth.model.AssetRecord;
import com.lemnt.member.domain.growth.model.AssetRecordPageFilter;
import java.util.List;
/** member_point_record独立存储边界，原业务码只作兼容幂等键。 */
public interface MemberPointRecordGateway {
    /** 读取原业务键对应的稳定积分流水。 */
    AssetRecord request(Long memberId,String code,String bizId);
    /** 保存积分及展示快照，不写经验表。 */
    void save(AssetRecord record);
    /** 查询当前会员昵称关联的积分分页。 */
    List<AssetRecord> page(AssetRecordPageFilter filter);
    /** 按相同筛选统计积分总数。 */
    long count(AssetRecordPageFilter filter);
    /** 按积分表编号读取详情。 */
    AssetRecord detail(Long id);
    /** 汇总指定订单已退回的同类积分。 */
    long returned(Long memberId,String code,String orderNo);
}
