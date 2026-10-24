package com.lemnt.mall.domain.gateway;
import com.lemnt.mall.domain.model.Product;
public interface ProductGateway { Product findById(Long id); Product findByCode(String code); Product save(Product product); boolean updateStatus(Long id,String status); boolean reserveStock(Long id,Integer quantity); void releaseStock(Long id,Integer quantity); }