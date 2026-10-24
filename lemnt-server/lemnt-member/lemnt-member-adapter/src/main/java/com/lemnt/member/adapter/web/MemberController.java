package com.lemnt.member.adapter.web;
import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.member.application.cmd.MemberRegisterCmd;
import com.lemnt.member.application.executor.MemberRegisterCmdExe;
import com.lemnt.member.domain.gateway.MemberGateway;
import com.lemnt.member.domain.model.Member;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/members") @RequiredArgsConstructor
public class MemberController {
 private final MemberRegisterCmdExe registerExe; private final MemberGateway memberGateway;
 public record RegisterReq(@NotBlank String nickname,String mobile,@Email String email,String avatar){}
 public record MemberRes(Long id,String memberNo,String nickname,String mobile,String email,String avatar,Integer status,Long points){static MemberRes from(Member m){return new MemberRes(m.id(),m.memberNo(),m.nickname(),m.mobile(),m.email(),m.avatar(),m.status(),m.points());}}
 @PostMapping("/register") public R<MemberRes> register(@Valid @RequestBody RegisterReq req){return R.ok(MemberRes.from(registerExe.execute(new MemberRegisterCmd(req.nickname(),req.mobile(),req.email(),req.avatar()))));}
 @GetMapping("/{id}") @RequiresPermissions("member:query") public R<MemberRes> detail(@PathVariable Long id){Member m=memberGateway.findById(id);return m==null?R.fail("会员不存在"):R.ok(MemberRes.from(m));}
}