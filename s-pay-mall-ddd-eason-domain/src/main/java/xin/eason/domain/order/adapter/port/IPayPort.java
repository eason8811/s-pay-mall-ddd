package xin.eason.domain.order.adapter.port;

import xin.eason.domain.order.model.aggregate.OrderAggregate;
import xin.eason.domain.order.model.entity.MarketPayDiscountEntity;

import java.time.LocalDateTime;
import java.util.Map;

public interface IPayPort {
    /**
     * 发送用户购买的商品发货成功的信息
     * @param orderAggregate 订单聚合
     */
    void sendSendSuccessTemplate(OrderAggregate orderAggregate);

    /**
     * 向指定用户发送模板信息
     *
     * @param templateId  需要发送的模板信息的 <b>模板 ID</b>
     * @param openid      需要发送的用户的 <b>openId</b>
     * @param turnToUrl   发送给用户的模板信息点击后跳转的 <b>URL</b>
     * @param accessToken 用于向微信公众平台调用 API 的 <b>Access Token</b>
     * @param dataMap 用于存储数据的键值对
     * @return 如果发送成功则返回模板信息的 <b>ID</b>
     */
    Long sendTemplate(String templateId, String openid, String turnToUrl, String accessToken, Map<String, String> dataMap);

    /**
     * 进行拼团订单锁定
     * @param activityId 活动 ID
     * @param teamId 拼团队伍 ID
     * @param userId 用户 ID
     * @param productId 商品 ID
     * @param orderItemId 本地订单明细 ID
     * @return 拼团订单支付折扣实体
     */
    MarketPayDiscountEntity lockMarketOrder(Long activityId, String teamId, String userId, String productId, String orderItemId);

    /**
     * 进行拼团订单结算
     *
     * @param userId  用户 ID
     * @param tradeNo 本地订单明细 ID
     * @param payTime 支付时间
     * @return 支付成功的订单 ID
     */
    String settlementMarketOrder(String userId, String tradeNo, LocalDateTime payTime);
}
