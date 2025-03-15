package xin.eason.api.dto;

import lombok.Data;

import java.util.List;

/**
 * 拼团系统回调数据传输类
 */
@Data
public class GroupBuyNotifyDTO {
    /**
     * 已经达成拼团目标的队伍 ID
     */
    private String teamId;
    /**
     * 对与拼团系统来说的外部订单 ID 列表 == 对于本系统来说的本地订单 ID 列表
     */
    private List<String> outerOrderId;
}
