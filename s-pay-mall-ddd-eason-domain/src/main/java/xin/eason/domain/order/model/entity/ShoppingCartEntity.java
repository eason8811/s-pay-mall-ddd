package xin.eason.domain.order.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xin.eason.domain.order.model.valobj.MarketType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCartEntity {
    private String teamId;
    private Long activityId;
    private MarketType marketType;
    private String userId;
    private String productId;
}
