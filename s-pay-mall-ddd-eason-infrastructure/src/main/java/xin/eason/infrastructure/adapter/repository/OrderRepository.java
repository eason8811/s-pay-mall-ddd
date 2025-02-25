package xin.eason.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xin.eason.domain.order.adapter.repository.IOrderRepository;
import xin.eason.domain.order.model.aggregate.OrderAggregate;
import xin.eason.domain.order.model.entity.OrderItemEntity;
import xin.eason.domain.order.model.entity.PayOrderEntity;
import xin.eason.domain.order.model.entity.ProductEntity;
import xin.eason.domain.order.model.entity.ShoppingCartEntity;
import xin.eason.domain.order.model.valobj.OrderStatusVO;
import xin.eason.infrastructure.dao.OrderMapper;
import xin.eason.infrastructure.dao.po.PayOrder;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderRepository implements IOrderRepository {

    private final OrderMapper orderMapper;

    /**
     * 查询未支付的订单，返回订单聚合
     *
     * @param shopCartEntity 购物车实体对象
     * @return 订单聚合
     */
    @Override
    public OrderAggregate queryUnPayOrder(ShoppingCartEntity shopCartEntity) {
        PayOrder payOrder = PayOrder.builder()
                .userId(shopCartEntity.getUserId())
                .productId(shopCartEntity.getProductId())
                .status(OrderStatusVO.PAY_WAITE.getCode())
                .build();
        LambdaQueryWrapper<PayOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PayOrder::getUserId, payOrder.getUserId()).eq(PayOrder::getProductId, payOrder.getProductId()).eq(PayOrder::getStatus, payOrder.getStatus());
        PayOrder unPayOrder = orderMapper.selectOne(wrapper);
        if (unPayOrder == null) {
            return OrderAggregate.builder().build();
        }
        return OrderAggregate.builder()
                .userId(unPayOrder.getUserId())
                .payUrl(unPayOrder.getPayUrl())
                .productEntity(
                        ProductEntity.builder()
                                .productId(unPayOrder.getProductId())
                                .productName(unPayOrder.getProductName())
                                .price(unPayOrder.getTotalAmount())
                                .build()
                )
                .orderItemEntity(
                        OrderItemEntity.builder()
                                .orderItemId(unPayOrder.getOrderId())
                                .orderStatus(OrderStatusVO.valueOf(unPayOrder.getStatus()))
                                .orderTime(unPayOrder.getOrderTime())
                                .totalAmount(unPayOrder.getTotalAmount())
                                .productName(unPayOrder.getProductName())
                                .productId(unPayOrder.getProductId())
                                .build()
                )
                .build();
    }

    /**
     * 储存订单信息
     *
     * @param orderAggregate 订单聚合
     */
    @Override
    public void doSaveOrder(OrderAggregate orderAggregate) {
        OrderItemEntity orderItemEntity = orderAggregate.getOrderItemEntity();
        ProductEntity productEntity = orderAggregate.getProductEntity();
        PayOrder payOrder = PayOrder.builder()
                .userId(orderAggregate.getUserId())
                .productId(productEntity.getProductId())
                .productName(productEntity.getProductName())
                .orderId(orderItemEntity.getOrderItemId())
                .orderTime(orderItemEntity.getOrderTime())
                .totalAmount(orderItemEntity.getTotalAmount())
                .status(orderItemEntity.getOrderStatus().getCode())
                .payUrl(orderAggregate.getPayUrl())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        orderMapper.insert(payOrder);
    }

    /**
     * 更新支付单信息
     *
     * @param payOrderEntity 支付单实体
     */
    @Override
    public void updateOrderPayInfo(PayOrderEntity payOrderEntity) {
        PayOrder payOrder = PayOrder.builder()
                .userId(payOrderEntity.getUserId())
                .orderId(payOrderEntity.getOrderId())
                .payUrl(payOrderEntity.getPayUrl())
                .status(payOrderEntity.getOrderStatus().getCode())
                .build();
        LambdaUpdateWrapper<PayOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PayOrder::getOrderId, payOrder.getOrderId());
        orderMapper.update(payOrder, updateWrapper);
    }

    /**
     * 将指定订单编号的订单付款状态修改为支付成功
     * @param tradeNo 订单编号
     */
    @Override
    public void changeOrderStatusSuccess(String tradeNo) {
        LambdaUpdateWrapper<PayOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PayOrder::getOrderId, tradeNo)
                        .set(PayOrder::getStatus, OrderStatusVO.PAY_SUCCESS);
        orderMapper.update(updateWrapper);
    }

    /**
     * 根据订单 ID 查询订单信息
     *
     * @param orderId 订单 ID
     * @return 订单聚合
     */
    @Override
    public OrderAggregate queryOrderById(String orderId) {
        PayOrder payOrder = orderMapper.selectOne(new LambdaQueryWrapper<PayOrder>().eq(PayOrder::getOrderId, orderId));
        return OrderAggregate.builder()
                .userId(payOrder.getUserId())
                .payUrl(payOrder.getPayUrl())
                .productEntity(
                        ProductEntity.builder()
                                .productId(payOrder.getProductId())
                                .productName(payOrder.getProductName())
                                .price(payOrder.getTotalAmount())
                                .build()
                )
                .orderItemEntity(
                        OrderItemEntity.builder()
                                .orderTime(payOrder.getOrderTime())
                                .orderItemId(payOrder.getOrderId())
                                .orderStatus(OrderStatusVO.valueOf(payOrder.getStatus()))
                                .totalAmount(payOrder.getTotalAmount())
                                .productId(payOrder.getProductId())
                                .productName(payOrder.getProductName())
                                .build()
                )
                .build();
    }
}
