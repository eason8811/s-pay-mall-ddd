package xin.eason.domain.order.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 营销支付折扣实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketPayDiscountEntity {
    /**
     * 商品原始价格
     */
    private BigDecimal originalAmount;
    /**
     * 商品折扣金额
     */
    private BigDecimal deductionAmount;
    /**
     * 商品支付价格
     */
    private BigDecimal payAmount;
}
