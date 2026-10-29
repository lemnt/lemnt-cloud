package com.lemnt.mall.application.product.executor;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.application.product.cmd.CommentSelfCreateCmd;
import com.lemnt.mall.domain.product.gateway.*;
import com.lemnt.mall.domain.product.model.ProductComment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
/** 创建源定义的后台自评，会员0且不关联订单。 */
@Component
@RequiredArgsConstructor
public class CommentSelfCreateCmdExe {
    private final ProductCommentGateway comments;
    private final ProductSkuGateway skus;
    private final ProductSpuGateway spus;
    /** 校验输入、读取真实商品快照后保存；不调用会员或交易服务。 */
    public Long execute(CommentSelfCreateCmd cmd) {
        if (cmd == null || cmd.skuId() == null || cmd.skuId() <= 0
                || !text(cmd.userNickname(), 80) || !url(cmd.userAvatar()) || !text(cmd.content(), 1000)
                || !score(cmd.descriptionScores()) || !score(cmd.benefitScores())
                || (cmd.picUrls() != null && (cmd.picUrls().size() > 9 || !cmd.picUrls().stream().allMatch(this::url)))) {
            throw new ServiceException("自评参数不合法", 400);
        }
        var sku = skus.catalogSku(cmd.skuId());
        var spu = sku == null ? null : spus.findSpu(sku.spuId());
        if (sku == null || spu == null || "DELETED".equals(sku.status()) || "DELETED".equals(spu.status())) {
            throw new ServiceException("商品或SKU不存在", 404);
        }
        var properties = sku.properties().stream().map(v -> new ProductComment.Property(
                v.propertyId(), v.propertyName(), v.valueId(), v.valueName())).toList();
        return comments.save(new ProductComment(null, spu.id(), sku.id(), null, 0L,
                (cmd.descriptionScores() + cmd.benefitScores()) / 2, cmd.content().trim(), true,
                null, null, null, null, cmd.descriptionScores(), cmd.benefitScores(),
                cmd.picUrls() == null ? List.of() : cmd.picUrls(), false, cmd.userNickname().trim(), cmd.userAvatar(),
                null, spu.name(), sku.image(), properties, false, null)).id();
    }
    private boolean score(Integer value) { return value != null && value >= 1 && value <= 5; }
    private boolean text(String value, int max) { return value != null && !value.isBlank() && value.length() <= max; }
    private boolean url(String value) { return text(value, 1024) && value.matches("https://[^\\s]+"); }
}
