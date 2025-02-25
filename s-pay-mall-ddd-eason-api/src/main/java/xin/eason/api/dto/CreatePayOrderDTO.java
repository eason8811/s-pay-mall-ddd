package xin.eason.api.dto;

import lombok.Data;

/**
 * 用于创建订单的数据传输对象
 */
@Data
public class CreatePayOrderDTO {
    /**
     * 用户 ID
     */
    private String userId;
    /**
     * 产品 ID
     */
    private String productId;
}
