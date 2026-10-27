package com.lemnt.mall.adapter.web.product;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.application.product.cmd.ProductCreateCmd;
import com.lemnt.mall.application.product.executor.ProductCreateCmdExe;
import com.lemnt.mall.application.product.executor.ProductDetailQueryExe;
import com.lemnt.mall.application.product.executor.ProductStatusCmdExe;
import com.lemnt.mall.domain.product.model.Product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 商品接口。
 */
@RestController
@RequestMapping("/mall/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductCreateCmdExe createExe;
    private final ProductDetailQueryExe detailExe;
    private final ProductStatusCmdExe statusExe;

    /**
     * 创建请求。
     */
    public record CreateReq(
            @NotBlank
            String productCode,
            @NotBlank
            String name,
            String description,
            String mainImage,
            @NotNull
            @DecimalMin("0.01")
            BigDecimal price,
            @NotNull
            @Min(0)
            Integer stock
    ) {}

    /**
     * 商品响应。
     */
    public record ProductRes(
            Long id,
            String productCode,
            String name,
            BigDecimal price,
            Integer stock,
            String status
    ) {
        static ProductRes from(Product p) {
            return new ProductRes(p.id(), p.productCode(), p.name(), p.price(), p.stock(), p.status());
        }
    }

    /**
     * 创建。
     */
    @PostMapping
    @RequiresPermissions("mall:product:add")
    public R<ProductRes> create(@Valid @RequestBody CreateReq req) {
        return R.ok(ProductRes.from(createExe.execute(new ProductCreateCmd(req.productCode(),
                req.name(),
                req.description(),
                req.mainImage(),
                req.price(),
                req.stock()))));
    }

    /**
     * 详情。
     */
    @GetMapping("/{id}")
    @RequiresPermissions("mall:product:query")
    public R<ProductRes> detail(@PathVariable Long id) {
        Product p=detailExe.execute(id);
        return p==null?R.fail("商品不存在"):R.ok(ProductRes.from(p));
    }

    /**
     * 上架状态。
     */
    @PutMapping("/{id}/shelf")
    @RequiresPermissions("mall:product:edit")
    public R<Boolean> shelf(@PathVariable Long id, @RequestParam boolean onShelf) {
        return R.ok(statusExe.execute(id, onShelf));
    }
}
