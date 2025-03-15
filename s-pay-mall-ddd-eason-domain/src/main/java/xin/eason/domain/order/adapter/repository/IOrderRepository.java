package xin.eason.domain.order.adapter.repository;

import xin.eason.domain.order.model.aggregate.OrderAggregate;
import xin.eason.domain.order.model.entity.PayOrderEntity;
import xin.eason.domain.order.model.entity.ShoppingCartEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface IOrderRepository {

    /**
     * 查询未支付的订单，返回订单聚合
     * @param shopCartEntity 购物车实体对象
     * @return 订单聚合
     */
    OrderAggregate queryUnPayOrder(ShoppingCartEntity shopCartEntity);

    /**
     * 储存订单信息
     * @param orderAggregate 订单聚合
     */
    void doSaveOrder(OrderAggregate orderAggregate);

    /**
     * 更新支付单信息
     * @param payOrderEntity 支付单实体
     */
    void updateOrderPayInfo(PayOrderEntity payOrderEntity);

    /**
     * 将指定订单编号的订单付款状态修改为支付成功
     * @param orderIdList 订单编号列表
     * @param payTime 支付时间
     */
    void changeOrderStatusSuccess(List<String> orderIdList, LocalDateTime payTime);

    /**
     * 根据订单 ID 查询订单信息
     *
     * @param orderId 订单 ID
     * @return 订单聚合
     */
    OrderAggregate queryOrderById(String orderId);
}
