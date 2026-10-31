package com.lemnt.member.adapter.app.address;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.member.adapter.app.address.converter.MemberAddressReqConverter;
import com.lemnt.member.adapter.app.address.converter.MemberAddressResConverter;
import com.lemnt.member.adapter.app.address.req.AppMemberAddressCreateReq;
import com.lemnt.member.adapter.app.address.req.AppMemberAddressUpdateReq;
import com.lemnt.member.adapter.app.address.res.AppMemberAddressCreateRes;
import com.lemnt.member.adapter.app.address.res.AppMemberAddressDefaultAddressRes;
import com.lemnt.member.adapter.app.address.res.AppMemberAddressDetailRes;
import com.lemnt.member.adapter.app.address.res.AppMemberAddressListRes;
import com.lemnt.member.adapter.app.address.res.AppMemberAddressUpdateRes;
import com.lemnt.member.application.address.cmd.AddressDeleteCmd;
import com.lemnt.member.application.address.executor.MemberAddressDefaultQueryExe;
import com.lemnt.member.application.address.executor.MemberAddressDeleteCmdExe;
import com.lemnt.member.application.address.executor.MemberAddressDetailQueryExe;
import com.lemnt.member.application.address.executor.MemberAddressListQueryExe;
import com.lemnt.member.application.address.executor.MemberAddressSaveCmdExe;
import com.lemnt.member.application.address.qry.AddressDefaultQry;
import com.lemnt.member.application.address.qry.AddressDetailQry;
import com.lemnt.member.application.address.qry.AddressListQry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 小程序会员地址簿。
 */
@Tag(name = "小程序会员地址簿")
@RestController
@RequestMapping("/app/addresses")
@RequiredArgsConstructor
public class AppMemberAddressController {

    private final MemberAddressDeleteCmdExe memberAddressDeleteCmdExe;
    private final MemberAddressDetailQueryExe memberAddressDetailQueryExe;
    private final MemberAddressListQueryExe memberAddressListQueryExe;
    private final MemberAddressSaveCmdExe memberAddressSaveCmdExe;
    private final MemberAddressDefaultQueryExe memberAddressDefaultQueryExe;

    @GetMapping
    @Operation(summary = "本人地址列表")
    public R<List<AppMemberAddressListRes>> list() {
        return R.ok(
                memberAddressListQueryExe.execute(new AddressListQry(SecurityUtils.getUserId())).stream()
                        .map(MemberAddressResConverter::toAppMemberAddressListRes)
                        .toList());
    }

    @GetMapping("/default")
    @Operation(summary = "本人默认地址")
    public R<AppMemberAddressDefaultAddressRes> defaultAddress() {
        return R.ok(
                MemberAddressResConverter.toAppMemberAddressDefaultAddressRes(
                        memberAddressDefaultQueryExe.execute(new AddressDefaultQry(SecurityUtils.getUserId()))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "本人地址详情，不存在和他人地址均为空")
    public R<AppMemberAddressDetailRes> detail(
            @PathVariable Long id) {
        return R.ok(
                MemberAddressResConverter.toAppMemberAddressDetailRes(
                        memberAddressDetailQueryExe.execute(new AddressDetailQry(SecurityUtils.getUserId(), id))));
    }

    @PostMapping
    @Operation(summary = "新增本人地址")
    public R<AppMemberAddressCreateRes> create(
            @Valid @RequestBody AppMemberAddressCreateReq req) {
        return R.ok(
                MemberAddressResConverter.toAppMemberAddressCreateRes(
                        memberAddressSaveCmdExe.execute(
                                MemberAddressReqConverter.address(SecurityUtils.getUserId(), null, req))));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新本人地址，默认地址修改串行化")
    public R<AppMemberAddressUpdateRes> update(
            @PathVariable Long id,
            @Valid @RequestBody AppMemberAddressUpdateReq req) {
        return R.ok(
                MemberAddressResConverter.toAppMemberAddressUpdateRes(
                        memberAddressSaveCmdExe.execute(
                                MemberAddressReqConverter.address(SecurityUtils.getUserId(), id, req))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除本人地址")
    public R<Boolean> delete(
            @PathVariable Long id) {
        return R.ok(memberAddressDeleteCmdExe.execute(new AddressDeleteCmd(SecurityUtils.getUserId(), id)));
    }
}
