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
    /**
     * 队伍 ID
     */
    private String teamId;
    /**
     * 活动 ID
     */
    private Long activityId;
    /**
     * 营销类型
     */
    private Integer marketType;
}
