package com.lemnt.member.infrastructure.gateway;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lemnt.member.domain.gateway.MemberGateway;
import com.lemnt.member.domain.model.Member;
import com.lemnt.member.infrastructure.dataobject.MemberDO;
import com.lemnt.member.infrastructure.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
@Component @RequiredArgsConstructor
public class MemberGatewayImpl implements MemberGateway {
 private final MemberMapper mapper;
 public Member findById(Long id){return toDomain(mapper.selectById(id));}
 public Member findByMobile(String mobile){if(mobile==null||mobile.isBlank())return null;return toDomain(mapper.selectOne(Wrappers.lambdaQuery(MemberDO.class).eq(MemberDO::getMobile,mobile.trim()).last("LIMIT 1")));}
 public Member findByEmail(String email){if(email==null||email.isBlank())return null;return toDomain(mapper.selectOne(Wrappers.lambdaQuery(MemberDO.class).eq(MemberDO::getEmail,email.trim()).last("LIMIT 1")));}
 public Member save(Member m){MemberDO d=new MemberDO().setMemberNo(m.memberNo()).setNickname(m.nickname()).setMobile(m.mobile()).setEmail(m.email()).setAvatar(m.avatar()).setStatus(m.status()).setPoints(m.points());mapper.insert(d);return toDomain(d);}
 private Member toDomain(MemberDO d){return d==null?null:new Member(d.getId(),d.getMemberNo(),d.getNickname(),d.getMobile(),d.getEmail(),d.getAvatar(),d.getStatus(),d.getPoints(),d.getCreateTime());}
}