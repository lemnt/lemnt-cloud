package com.lemnt.mall.application.product.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.application.product.cmd.CommentCreateCmd;
import com.lemnt.mall.application.product.cmd.CommentReplyCmd;
import com.lemnt.mall.domain.member.gateway.MemberLookupGateway;
import com.lemnt.mall.domain.product.gateway.ProductCommentGateway;
import com.lemnt.mall.domain.product.gateway.ProductSkuGateway;
import com.lemnt.mall.domain.product.model.ProductComment;
import com.lemnt.mall.domain.settlement.gateway.OrderGateway;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/** 评价来自本人完成订单；订单、会员资料失败时不写入半份评价。 */
@Component
@RequiredArgsConstructor
public class ProductCommentService {
    private final ProductCommentGateway productCommentGateway;
    private final ProductSkuGateway productSkuGateway;
    private final OrderGateway orderGateway;
    private final MemberLookupGateway memberLookupGateway;

    /** 兼容HTTP入口委托交易持锁核验，只有两侧状态确认后才返回成功。 */
    public ProductComment create(CommentCreateCmd cmd) {
        validateIdentity(cmd);
        var input = normalize(cmd);
        Long id = orderGateway.createComment(new com.lemnt.mall.domain.product.model.CommentOrderRequest(
                cmd.memberId(), cmd.orderNo().trim(), cmd.skuId(), cmd.orderItemId(), cmd.score(), input.content(),
                cmd.descriptionScores(), cmd.benefitScores(), input.anonymous(), input.pictures()));
        require(id != null && id > 0, "评价结果未确认", 503);
        var saved = productCommentGateway.find(id);
        require(saved != null && cmd.memberId().equals(saved.memberId()) && cmd.skuId().equals(saved.skuId())
                && cmd.orderNo().trim().equals(saved.orderNo())
                && (cmd.orderItemId() == null || cmd.orderItemId().equals(saved.orderItemId())), "评价结果未确认", 503);
        return saved;
    }

    /** 私有RPC只接收交易已验证的订单事实，不回查持锁中的交易订单。 */
    public ProductComment saveVerified(com.lemnt.mall.application.product.cmd.CommentSaveCmd purchase) {
        return savePurchase(purchase, false);
    }
    /** 独立私有系统入口，不能由会员HTTP输入开启空正文模式。 */
    public ProductComment saveSystem(com.lemnt.mall.application.product.cmd.CommentSaveCmd purchase) {
        require(purchase != null && Integer.valueOf(5).equals(purchase.score())
                && Integer.valueOf(5).equals(purchase.descriptionScores()) && Integer.valueOf(5).equals(purchase.benefitScores())
                && "".equals(purchase.content()) && Boolean.FALSE.equals(purchase.anonymous())
                && purchase.picUrls() != null && purchase.picUrls().isEmpty(), "系统评价参数不合法", 400);
        return savePurchase(purchase, true);
    }
    private ProductComment savePurchase(com.lemnt.mall.application.product.cmd.CommentSaveCmd purchase, boolean system) {
        require(purchase != null && purchase.orderId() != null && purchase.orderId() > 0
                && purchase.orderItemId() != null && purchase.orderItemId() > 0, "真实订单项信息缺失", 400);
        var cmd = new CommentCreateCmd(purchase.memberId(), purchase.orderNo(), purchase.skuId(), purchase.score(),
                purchase.content(), purchase.descriptionScores(), purchase.benefitScores(), purchase.anonymous(), purchase.picUrls(), purchase.orderItemId());
        validateIdentity(cmd);
        var input = normalize(cmd, system);
        var existing = productCommentGateway.findByOrderItem(purchase.orderItemId());
        if (existing != null) {
            require(purchase.orderId().equals(existing.orderId()), "评价订单项归属不一致", 409);
            return requireSame(existing, cmd, input);
        }
        var legacy = productCommentGateway.findByOrderSku(cmd.orderNo().trim(), cmd.skuId());
        if (legacy != null) {
            require(purchase.uniqueSku(), "历史评价无法唯一定位订单项", 409);
            requireSame(legacy, cmd, input);
            try {
                require(productCommentGateway.bindOrderItem(new com.lemnt.mall.domain.product.model.CommentOrderLink(
                        legacy.id(), purchase.orderId(), purchase.orderItemId())), "评价订单项绑定冲突", 409);
                return productCommentGateway.find(legacy.id());
            } catch (DuplicateKeyException conflict) {
                return winner(purchase.orderItemId(), cmd, input);
            }
        }
        require(purchase.allowCreate(), "订单项已经评价，不能重新投稿", 409);
        var sku = purchase.spuId() == null ? productSkuGateway.catalogSku(cmd.skuId()) : null;
        Long spuId = purchase.spuId() != null ? purchase.spuId() : sku == null ? null : sku.spuId();
        require(spuId != null, "订单商品标识不存在", 400);
        var member = memberLookupGateway.findById(cmd.memberId());
        require(member != null && cmd.memberId().equals(member.id()), "会员资料不存在", 404);
        var properties = purchase.properties() == null ? null : purchase.properties().stream().map(property ->
                new ProductComment.Property(property.propertyId(), property.propertyName(), property.valueId(), property.valueName())).toList();
        try {
            return productCommentGateway.save(new ProductComment(null, spuId, cmd.skuId(), cmd.orderNo().trim(), cmd.memberId(), input.score(),
                    input.content(), true, null, null, null, null,
                    input.description(), input.benefit(), input.pictures(), input.anonymous(), member.nickname(), member.avatar(),
                    purchase.orderId(), purchase.spuName(), purchase.skuPicUrl(), properties, false, purchase.orderItemId()));
        } catch (DuplicateKeyException conflict) {
            return winner(purchase.orderItemId(), cmd, input);
        }
    }

    private ProductComment winner(Long orderItemId, CommentCreateCmd cmd, Input input) {
        var saved = productCommentGateway.findByOrderItem(orderItemId);
        require(saved != null, "该订单商品已评价，不能重复评价", 409);
        return requireSame(saved, cmd, input);
    }

    private void validateIdentity(CommentCreateCmd cmd) {
        require(cmd != null, "评价不能为空", 400);
        require(cmd.memberId() != null && cmd.memberId() > 0, "请先登录", 401);
        require(cmd.skuId() != null && cmd.skuId() > 0, "SKU编号不合法", 400);
        require(cmd.orderNo() != null && !cmd.orderNo().isBlank() && cmd.orderNo().length() <= 40
                && (cmd.orderItemId() == null || cmd.orderItemId() > 0), "订单或订单项编号不合法", 400);
    }

    private ProductComment requireSame(ProductComment value, CommentCreateCmd cmd, Input input) {
        require(cmd.memberId().equals(value.memberId()) && cmd.skuId().equals(value.skuId())
                && cmd.orderNo().trim().equals(value.orderNo())
                && (value.orderItemId() == null || cmd.orderItemId() == null || cmd.orderItemId().equals(value.orderItemId())), "只能评价本人已完成订单内的商品", 400);
        boolean sameDimensions = value.descriptionScores() == null && value.benefitScores() == null
                ? cmd.descriptionScores() == null && cmd.benefitScores() == null
                : Objects.equals(value.descriptionScores(), input.description()) && Objects.equals(value.benefitScores(), input.benefit());
        require(value.score() == input.score() && input.content().equals(value.content()) && sameDimensions
                && Boolean.TRUE.equals(value.anonymous()) == input.anonymous()
                && Objects.equals(value.picUrls() == null ? List.of() : value.picUrls(), input.pictures()),
                "该订单商品已评价，不能改写或重复评价", 409);
        return value;
    }

    /** 两项评分取平均向下取整；旧单分输入明确作为两项相同分数。 */
    private Input normalize(CommentCreateCmd cmd) { return normalize(cmd, false); }
    private Input normalize(CommentCreateCmd cmd, boolean system) {
        Integer description = cmd.descriptionScores();
        Integer benefit = cmd.benefitScores();
        if (description == null && benefit == null) {
            description = cmd.score();
            benefit = cmd.score();
        }
        require(description != null && description >= 1 && description <= 5
                && benefit != null && benefit >= 1 && benefit <= 5, "描述和服务评分必须为1至5分", 400);
        int score = (description + benefit) / 2;
        require(cmd.score() == null || cmd.score() == score, "综合评分必须与两项评分一致", 400);
        require(cmd.content() != null && (system || !cmd.content().isBlank()) && cmd.content().length() <= 1000, "评价正文不合法", 400);
        var pictures = cmd.picUrls() == null ? List.<String>of() : cmd.picUrls();
        require(pictures.size() <= 9 && pictures.stream().allMatch(url -> url != null && url.length() <= 1024
                && url.matches("https://[^\\s]+")), "评价图片最多9张且须为HTTPS地址", 400);
        return new Input(score, description, benefit, Boolean.TRUE.equals(cmd.anonymous()), List.copyOf(pictures), cmd.content().trim());
    }

    /** 商家回复不更新评价快照；相同内容重试保留首次回复时间。 */
    @Transactional(rollbackFor = Exception.class)
    public ProductComment reply(CommentReplyCmd cmd) {
        require(cmd != null && cmd.id() != null && cmd.id() > 0, "评价编号不合法", 400);
        require(cmd.operatorId() != null && cmd.operatorId() > 0, "请先登录", 401);
        require(cmd.content() != null && !cmd.content().isBlank() && cmd.content().length() <= 1000, "回复内容不合法", 400);
        String content = cmd.content().trim();
        var value = productCommentGateway.lock(cmd.id());
        require(value != null, "评价不存在", 404);
        if (content.equals(value.replyContent())) {
            return value;
        }
        // 网关只写这三个回复字段及replyStatus，不将旧构造器的空快照写回。
        var updated = new ProductComment(value.id(), value.spuId(), value.skuId(), value.orderNo(), value.memberId(),
                value.score(), value.content(), value.visible(), content, cmd.operatorId(), LocalDateTime.now(), value.createTime());
        require(productCommentGateway.reply(updated), "回复保存失败", 409);
        return productCommentGateway.find(value.id());
    }

    private void require(boolean valid, String message, int code) {
        if (!valid) {
            throw new ServiceException(message, code);
        }
    }

    private record Input(int score, int description, int benefit, boolean anonymous, List<String> pictures, String content) {}
}
