package xin.eason.domain.order.model.entity;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品实体
 */
@Data
@Builder
public class ProductEntity {
    private String productId;
    private String productName;
    private String productDesc;
    private BigDecimal price;
}
