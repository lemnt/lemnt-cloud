package com.lemnt.mall.infrastructure.gateway;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;import com.lemnt.mall.domain.gateway.ProductGateway;import com.lemnt.mall.domain.model.Product;import com.lemnt.mall.infrastructure.dataobject.ProductDO;import com.lemnt.mall.infrastructure.mapper.ProductMapper;import lombok.RequiredArgsConstructor;import org.springframework.stereotype.Component;
@Component @RequiredArgsConstructor public class ProductGatewayImpl implements ProductGateway {private final ProductMapper mapper;
 public Product findById(Long id){return toDomain(mapper.selectById(id));}
 public Product findByCode(String code){if(code==null||code.isBlank())return null;return toDomain(mapper.selectOne(Wrappers.lambdaQuery(ProductDO.class).eq(ProductDO::getProductCode,code.trim()).last("LIMIT 1")));}
 public Product save(Product p){ProductDO d=new ProductDO().setProductCode(p.productCode()).setName(p.name()).setDescription(p.description()).setMainImage(p.mainImage()).setPrice(p.price()).setStock(p.stock()).setStatus(p.status());mapper.insert(d);return toDomain(d);}
 public boolean updateStatus(Long id,String status){return mapper.update(null,Wrappers.lambdaUpdate(ProductDO.class).eq(ProductDO::getId,id).set(ProductDO::getStatus,status))==1;}
 public boolean reserveStock(Long id,Integer qty){if(qty==null||qty<=0)return false;return mapper.reserveStock(id,qty)==1;}
 public void releaseStock(Long id,Integer qty){if(qty==null||qty<=0||mapper.releaseStock(id,qty)!=1)throw new IllegalStateException("库存补偿失败");}
 private Product toDomain(ProductDO d){return d==null?null:new Product(d.getId(),d.getProductCode(),d.getName(),d.getDescription(),d.getMainImage(),d.getPrice(),d.getStock(),d.getStatus(),d.getCreateTime());}
}