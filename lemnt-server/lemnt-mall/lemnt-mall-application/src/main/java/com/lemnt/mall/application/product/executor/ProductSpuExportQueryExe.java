package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.dto.ProductSpuDTO;
import com.lemnt.mall.application.product.qry.ProductSpuExportQry;
import com.lemnt.mall.application.product.qry.ProductSpuPageQry;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** 商品按当前筛选条件导出，不把当前一页当作完整结果。 */
@Component
@RequiredArgsConstructor
public class ProductSpuExportQueryExe {
    private static final int MAX_EXPORT_ROWS = 10000;
    private final ProductSpuPageQueryExe pageQuery;

    /** 在同一只读事务中分页读取，超限或不完整时拒绝输出截断文件。 */
    @Transactional(readOnly = true)
    public List<ProductSpuDTO> execute(ProductSpuExportQry query) {
        if (query == null) {
            throw new IllegalArgumentException("导出条件不能为空");
        }
        var rows = new ArrayList<ProductSpuDTO>();
        long total;
        int page = 1;
        do {
            var result = pageQuery.execute(new ProductSpuPageQry(query.name(), query.status(), page++, 100,
                    query.categoryId(), query.brandId(), query.stockStatus()));
            total = result.total();
            if (total > MAX_EXPORT_ROWS) {
                throw new IllegalArgumentException("最多导出10000件商品，请缩小筛选范围");
            }
            rows.addAll(result.records());
            if (rows.size() < total && result.records().isEmpty()) {
                throw new IllegalStateException("商品导出读取不完整，请重试");
            }
        } while (rows.size() < total);
        if (rows.size() != total) {
            throw new IllegalStateException("商品导出结果发生变化，请重试");
        }
        return List.copyOf(rows);
    }
}
