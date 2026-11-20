package com.lemnt.member.infrastructure.user.gateway;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.domain.user.gateway.MemberWechatGateway;
import com.lemnt.member.infrastructure.user.dataobject.MemberWechatDO;
import com.lemnt.member.infrastructure.user.mapper.MemberWechatMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * member_wechat逐会员存储，openid 全库唯一。
 */
@Component
@RequiredArgsConstructor
public class MemberWechatGatewayImpl implements MemberWechatGateway {

    private final MemberWechatMapper memberWechatMapper;

    @Override
    public String findOpenId(Long memberId) {
        if (memberId == null) {
            return null;
        }
        MemberWechatDO row = memberWechatMapper.selectById(memberId);
        return row == null ? null : row.getOpenId();
    }

    /**
     * 同一会员再次登录覆盖原 openid；openid 已属于其他会员时拒绝，避免串号支付。
     */
    @Override
    public void bind(Long memberId, String openId) {
        if (memberId == null || openId == null || openId.isBlank()) {
            throw new ServiceException("微信标识不合法", 400);
        }
        MemberWechatDO owner =
            memberWechatMapper.selectOne(
                Wrappers.<MemberWechatDO>lambdaQuery()
                    .eq(MemberWechatDO::getOpenId, openId));
        if (owner != null && !memberId.equals(owner.getMemberId())) {
            throw new ServiceException("该微信已绑定其他会员", 400);
        }
        MemberWechatDO value = new MemberWechatDO().setMemberId(memberId).setOpenId(openId);
        if (memberWechatMapper.selectById(memberId) == null) {
            memberWechatMapper.insert(value);
        } else {
            memberWechatMapper.updateById(value);
        }
    }
}
