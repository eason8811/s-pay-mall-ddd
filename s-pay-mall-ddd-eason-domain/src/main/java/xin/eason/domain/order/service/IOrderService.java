package xin.eason.domain.order.service;

import xin.eason.domain.order.model.entity.PayOrderEntity;
import xin.eason.domain.order.model.entity.ShoppingCartEntity;


public interface IOrderService {
    /**
     * 创建订单
     * @param shoppingCartEntity 购物车实体对象
     * @return 支付单实体对象
     */
    PayOrderEntity createOrder(ShoppingCartEntity shoppingCartEntity) throws Exception;

    /**
     * 将指定订单编号的订单付款状态修改为支付成功
     * @param tradeNo 订单编号
     */
    void changeOrderStatusSuccess(String tradeNo);

    /**
     * 发送指定订单编号的订单支付成功的消息
     * @param tradeNo 订单编号
     */
    void sendPaySuccessEvent(String tradeNo);

    /**
     * 发送 发货成功 的模板信息
     * @param orderId 订单 ID
     */
    void sendDeliverySuccessMsg(String orderId);

    /**
     * 根据订单 ID 将订单状态修改为完成
     * @param orderId 订单 ID
     */
    void doneOrderById(String orderId);
}
