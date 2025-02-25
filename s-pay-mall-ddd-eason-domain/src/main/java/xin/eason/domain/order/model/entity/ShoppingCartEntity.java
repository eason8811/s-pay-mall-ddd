package xin.eason.domain.order.model.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShoppingCartEntity {
    private String userId;
    private String productId;
}
