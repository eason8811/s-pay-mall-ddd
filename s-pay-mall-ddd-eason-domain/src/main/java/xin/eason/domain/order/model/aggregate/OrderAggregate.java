package xin.eason.domain.order.model.aggregate;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.RandomStringUtils;
import xin.eason.domain.order.model.entity.OrderItemEntity;
import xin.eason.domain.order.model.entity.ProductEntity;
import xin.eason.domain.order.model.valobj.MarketType;
import xin.eason.domain.order.model.valobj.OrderStatusVO;

import java.time.LocalDateTime;

/**
 * 订单聚合
 */
@Data
@Builder
public class OrderAggregate {
    private String userId;
    private ProductEntity productEntity;
    private OrderItemEntity orderItemEntity;
    private String payUrl;

    public static OrderItemEntity createOrderItem(String productId, String productName, MarketType marketType) {
        return OrderItemEntity.builder()
                .orderItemId(RandomStringUtils.randomNumeric(12))
                .orderTime(LocalDateTime.now())
                .orderStatus(OrderStatusVO.CREATE)
                .productName(productName)
                .productId(productId)
                .marketType(marketType)
                .build();
    }
}
