package com.lemnt.member.domain.growth.gateway;

import com.lemnt.member.domain.growth.model.SignConfig;
import java.util.List;

/** member_sign_in_config表存储边界。 */
public interface MemberSignConfigGateway {
    /** 按天数升序列出规则。 */
    List<SignConfig> signConfigs();
    /** 按真实规则ID读取。 */
    SignConfig signConfig(Long id);
    /** 按唯一业务天数读取。 */
    SignConfig signConfigByDay(Integer day);
    /** 插入新规则，返回生成编号。 */
    Long create(SignConfig value);
    /** 按ID更新已有规则。 */
    boolean update(SignConfig value);
    /** 按ID删除规则。 */
    boolean signConfigDelete(Long id);
}
