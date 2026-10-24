package com.lemnt.mall.client;
import com.lemnt.mall.client.dto.ProductSnapshotDTO;
public interface ProductCatalogClient { ProductSnapshotDTO findOnShelf(Long productId); boolean reserveStock(Long productId,Integer quantity); void releaseStock(Long productId,Integer quantity); }