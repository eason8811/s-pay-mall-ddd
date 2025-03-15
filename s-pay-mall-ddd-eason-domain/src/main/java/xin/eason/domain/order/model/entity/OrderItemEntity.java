package xin.eason.domain.order.model.entity;

import lombok.Builder;
import lombok.Data;
import xin.eason.domain.order.model.valobj.MarketType;
import xin.eason.domain.order.model.valobj.OrderStatusVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单项目实体
 */
@Data
@Builder
public class OrderItemEntity {
    private String orderItemId;
    private String productId;
    private String productName;
    private LocalDateTime orderTime;
    private BigDecimal totalAmount;
    private BigDecimal deductionAmount;
    private BigDecimal payAmount;
    private MarketType marketType;
    private OrderStatusVO orderStatus;
}
