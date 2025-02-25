package xin.eason.infrastructure.adapter.port;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import xin.eason.domain.order.adapter.port.IProductPort;
import xin.eason.domain.order.model.entity.ProductEntity;
import xin.eason.infrastructure.gateway.ProductRPC;
import xin.eason.infrastructure.gateway.dto.ProductDTO;

@Component
@RequiredArgsConstructor
public class ProductPort implements IProductPort {

    private final ProductRPC productRPC;

    /**
     * 根据产品 ID 查询产品信息，返回产品实体对象
     * @param productId 产品 ID
     * @return 产品实体对象
     */
    @Override
    public ProductEntity queryProductByProductId(String productId) {
        ProductDTO productDTO = productRPC.queryProductByProductId(productId);
        return ProductEntity.builder()
                .productId(productDTO.getProductId())
                .productName(productDTO.getProductName())
                .productDesc(productDTO.getProductDesc())
                .price(productDTO.getPrice())
                .build();
    }
}
