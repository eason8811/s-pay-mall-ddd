package xin.eason.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xin.eason.domain.order.model.valobj.MarketType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单持久化对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayOrder {

    /**
     * 自增ID
     */
    private Long id;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 商品ID
     */
    private String productId;
    /**
     * 商品
     */
    private String productName;
    /**
     * 订单ID
     */
    private String orderId;
    /**
     * 下单时间
     */
    private LocalDateTime orderTime;
    /**
     * 订单金额
     */
    private BigDecimal totalAmount;
    /**
     * 订单状态；create-创建完成、pay_wait-等待支付、pay_success-支付成功、deal_done-交易完成、close-订单关单
     */
    private String status;
    /**
     * 支付信息
     */
    private String payUrl;
    /**
     * 支付时间
     */
    private LocalDateTime payTime;
    /**
     * 营销类型 (0, 无营销)  (1, 有营销)
     */
    private MarketType marketType;
    /**
     * 营销折扣价格
     */
    private BigDecimal marketDeductionAmount;
    /**
     * 营销支付价格
     */
    private BigDecimal payAmount;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    public static String cacheKey(String userId, String orderId) {
        return "small_" + userId + "_" + orderId;
    }

}
