package com.lemnt.product.client;

import com.lemnt.product.client.bo.StockReservationQueryBo;
import com.lemnt.product.client.dto.ProductSkuBriefDTO;
import com.lemnt.product.client.dto.ProductSnapshotDTO;
import com.lemnt.product.client.dto.ProductSpuBriefDTO;
import com.lemnt.product.client.dto.StockReservationDTO;

import java.util.List;

/**
 * 商品目录客户端。
 */
public interface ProductCatalogClient {
    /** 营销管理读取当前商品，包含未上架但尚未删除的目录记录。 */
    ProductSkuBriefDTO catalogSku(Long skuId);

    List<ProductSkuBriefDTO> catalogSkus(Long spuId);

    List<ProductSkuBriefDTO> skusBySpuIds(List<Long> spuIds);

    ProductSpuBriefDTO findSpu(Long spuId);

    List<ProductSpuBriefDTO> findSpus(List<Long> spuIds);

    /** 按原业务号读取预占事实，供订单核实库存后处理完成状态。 */
    StockReservationDTO reservationStatus(StockReservationQueryBo query);

    /** 按冻结售后数量回补原已确认库存，原售后号保证幂等。 */
    void returnStock(String orderNo, String afterSaleNo, Long skuId, Integer quantity);

    /**
     * 查找上架状态。
     */
    ProductSnapshotDTO findOnShelf(Long productId);

    /**
     * 查找批量上架状态。
     */
    List<ProductSnapshotDTO> findOnShelfBatch(List<Long> productIds);

    /**
     * 取消预留。
     */
    void cancelReservation(String reservationNo, Long productId, Integer quantity);

    /**
     * 预留库存。
     */
    boolean reserveStock(String reservationNo, Long productId, Integer quantity);

    /**
     * 释放库存。
     */
    void releaseStock(String reservationNo, Long productId);

    /**
     * 确认库存。
     */
    void confirmStock(String reservationNo, Long productId);
}
