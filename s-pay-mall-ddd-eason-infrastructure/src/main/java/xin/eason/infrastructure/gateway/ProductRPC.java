package xin.eason.infrastructure.gateway;

import xin.eason.infrastructure.gateway.dto.ProductDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 模拟RPC接口，商品库商品查询。
 */
@Service
public class ProductRPC {

    public ProductDTO queryProductByProductId(String productId) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductId(productId);
        productDTO.setProductName("<手写MyBatis：渐进式源码实践>");
        productDTO.setProductDesc("<手写MyBatis：渐进式源码实践>");
        productDTO.setPrice(new BigDecimal("100.00"));
        return productDTO;
    }

}
