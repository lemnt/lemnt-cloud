package com.lemnt.member.application.executor;
import com.lemnt.member.application.cmd.MemberRegisterCmd;
import com.lemnt.member.domain.gateway.MemberGateway;
import com.lemnt.member.domain.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
@Component @RequiredArgsConstructor
public class MemberRegisterCmdExe {
 private final MemberGateway gateway;
 @Transactional(rollbackFor=Exception.class)
 public Member execute(MemberRegisterCmd cmd) {
  if(cmd==null) throw new IllegalArgumentException("注册参数不能为空");
  if(cmd.mobile()!=null&&!cmd.mobile().isBlank()&&gateway.findByMobile(cmd.mobile().trim())!=null) throw new IllegalArgumentException("手机号已注册");
  if(cmd.email()!=null&&!cmd.email().isBlank()&&gateway.findByEmail(cmd.email().trim())!=null) throw new IllegalArgumentException("邮箱已注册");
  String no="M"+UUID.randomUUID().toString().replace("-","").substring(0,24).toUpperCase();
  return gateway.save(Member.register(no,cmd.nickname(),cmd.mobile(),cmd.email(),cmd.avatar()));
 }
}