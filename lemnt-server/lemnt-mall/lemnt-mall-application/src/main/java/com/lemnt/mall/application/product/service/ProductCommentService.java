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

    /** 原内容重试先核验保存结果；数据库唯一约束处理并发相同提交。 */
    public ProductComment create(CommentCreateCmd cmd) {
        require(cmd != null, "评价不能为空", 400);
        require(cmd.memberId() != null && cmd.memberId() > 0, "请先登录", 401);
        require(cmd.skuId() != null && cmd.skuId() > 0, "SKU编号不合法", 400);
        require(cmd.orderNo() != null && !cmd.orderNo().isBlank() && cmd.orderNo().length() <= 40, "订单编号不合法", 400);
        var input = normalize(cmd);
        String orderNo = cmd.orderNo().trim();
        var existing = productCommentGateway.findByOrderSku(orderNo, cmd.skuId());
        if (existing != null) {
            return requireSame(existing, cmd, input);
        }
        var order = orderGateway.findByOrderNo(orderNo);
        require(order != null && cmd.memberId().equals(order.memberId()) && "COMPLETED".equals(order.status()),
                "只能评价本人已完成订单内的商品", 400);
        var line = order.items().stream().filter(item -> cmd.skuId().equals(item.productId())).findFirst().orElse(null);
        require(line != null, "只能评价本人已完成订单内的商品", 400);
        var sku = line.spuId() == null ? productSkuGateway.catalogSku(cmd.skuId()) : null;
        Long spuId = line.spuId() != null ? line.spuId() : sku == null ? null : sku.spuId();
        require(spuId != null, "订单商品标识不存在", 400);
        var member = memberLookupGateway.findById(cmd.memberId());
        require(member != null && cmd.memberId().equals(member.id()), "会员资料不存在", 404);
        var properties = line.properties() == null ? null : line.properties().stream().map(property ->
                new ProductComment.Property(property.propertyId(), property.propertyName(), property.valueId(), property.valueName())).toList();
        try {
            return productCommentGateway.save(new ProductComment(null, spuId, cmd.skuId(), orderNo, cmd.memberId(), input.score(),
                    input.content(), true, null, null, null, null,
                    input.description(), input.benefit(), input.pictures(), input.anonymous(), member.nickname(), member.avatar(),
                    order.id(), line.spuName(), line.picUrl(), properties, false));
        } catch (DuplicateKeyException exception) {
            var winner = productCommentGateway.findByOrderSku(orderNo, cmd.skuId());
            if (winner == null) {
                throw exception;
            }
            return requireSame(winner, cmd, input);
        }
    }

    private ProductComment requireSame(ProductComment value, CommentCreateCmd cmd, Input input) {
        require(cmd.memberId().equals(value.memberId()), "只能评价本人已完成订单内的商品", 400);
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
    private Input normalize(CommentCreateCmd cmd) {
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
        require(cmd.content() != null && !cmd.content().isBlank() && cmd.content().length() <= 1000, "评价正文不合法", 400);
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
