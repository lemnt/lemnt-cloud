package com.lemnt.member.adapter.web.address;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.member.adapter.web.address.converter.MemberAddressResConverter;
import com.lemnt.member.adapter.web.address.res.AddressRes;
import com.lemnt.member.application.address.executor.MemberAddressListQueryExe;
import com.lemnt.member.application.address.qry.AddressListQry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 会员地址接口。 */
@Tag(name = "会员地址")
@RestController
@RequestMapping("/member-users")
@RequiredArgsConstructor
public class MemberUserAddressController {

    private final MemberAddressListQueryExe memberAddressListQueryExe;

    @GetMapping("/{id}/addresses")
    @Operation(summary = "后台查看会员地址簿，包含个人信息")
    @RequiresPermissions("member:address:query")
    public R<List<AddressRes>> addresses(@PathVariable Long id) {
        return R.ok(
                memberAddressListQueryExe.execute(new AddressListQry(id)).stream()
                .map(MemberAddressResConverter::address)
                .toList());
    }
}
