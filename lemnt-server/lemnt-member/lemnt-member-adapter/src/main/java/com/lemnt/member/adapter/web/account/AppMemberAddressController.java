package com.lemnt.member.adapter.web.account;

import com.lemnt.common.core.domain.R;
import com.lemnt.member.adapter.web.account.req.*;
import com.lemnt.member.adapter.web.account.res.*;
import com.lemnt.member.adapter.web.account.converter.*;
import com.lemnt.member.application.account.cmd.*;
import com.lemnt.member.application.account.qry.*;
import com.lemnt.member.application.account.executor.*;
import com.lemnt.member.application.account.service.MemberAuthService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Tag(name = "小程序会员地址簿")
@RestController
@RequestMapping("/member/app/addresses")
@RequiredArgsConstructor
public class AppMemberAddressController {
    private final MemberAuthService auth;
    private final MemberAccountCmdExe commands;
    private final MemberAccountQueryExe queries;

    @GetMapping
    @Operation(summary = "本人地址列表")
    public R<List<AddressRes>> list(
            @RequestHeader(value = "Authorization", required = false) String token) {
        return R.ok(
                queries.execute(new AddressListQry(auth.require(token))).stream()
                        .map(MemberResConverter::address)
                        .toList());
    }

    @GetMapping("/default")
    @Operation(summary = "本人默认地址")
    public R<AddressRes> defaultAddress(
            @RequestHeader(value = "Authorization", required = false) String token) {
        return R.ok(
                MemberResConverter.address(
                        queries.execute(new DefaultAddressQry(auth.require(token)))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "本人地址详情，不存在和他人地址均为空")
    public R<AddressRes> detail(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long id) {
        return R.ok(
                MemberResConverter.address(
                        queries.execute(new AddressDetailQry(auth.require(token), id))));
    }

    @PostMapping
    @Operation(summary = "新增本人地址，最多20个")
    public R<AddressRes> create(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody AddressSaveReq req) {
        return R.ok(
                MemberResConverter.address(
                        commands.execute(
                                MemberReqConverter.address(auth.require(token), null, req))));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新本人地址，默认地址修改串行化")
    public R<AddressRes> update(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long id,
            @Valid @RequestBody AddressSaveReq req) {
        return R.ok(
                MemberResConverter.address(
                        commands.execute(
                                MemberReqConverter.address(auth.require(token), id, req))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除本人地址")
    public R<Boolean> delete(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long id) {
        return R.ok(commands.execute(new AddressDeleteCmd(auth.require(token), id)));
    }
}
