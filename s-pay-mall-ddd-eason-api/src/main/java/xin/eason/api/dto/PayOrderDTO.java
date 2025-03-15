package xin.eason.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 支付单实体对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayOrderDTO {

    /** 用户ID */
    private String userId;
    /** 订单ID */
    private String orderId;
    /** 支付地址；创建支付后，获得支付信息；*/
    private String payUrl;
    /**
     * 折扣金额
     */
    private BigDecimal deductionAmount;
    /**
     * 支付价格
     */
    private BigDecimal payAmount;
    /**
     * 营销类型
     */
    private Integer marketType;
    /** 订单状态；0-创建完成、1-等待支付、2-支付成功、3-交易完成、4-订单关单 */
    private String orderStatus;

}
