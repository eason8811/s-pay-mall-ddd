package xin.eason.domain.order.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import xin.eason.domain.order.adapter.event.IPayEvent;
import xin.eason.domain.order.adapter.port.IPayPort;
import xin.eason.domain.order.adapter.port.IProductPort;
import xin.eason.domain.order.adapter.repository.IOrderRepository;
import xin.eason.domain.order.model.aggregate.OrderAggregate;
import xin.eason.domain.order.model.entity.OrderItemEntity;
import xin.eason.domain.order.model.entity.PayOrderEntity;
import xin.eason.domain.order.model.entity.ProductEntity;
import xin.eason.domain.order.model.entity.ShoppingCartEntity;
import xin.eason.domain.order.model.valobj.OrderStatusVO;

import java.math.BigDecimal;

@Slf4j
@AllArgsConstructor
public abstract class AbstractOrderService implements IOrderService{

    protected IOrderRepository repository;

    protected IProductPort productPort;

    protected IPayEvent payEvent;

    protected AlipayClient alipayClient;

    protected IPayPort payPort;

    /**
     * 创建订单
     * @param shoppingCartEntity 购物车实体对象
     * @return 支付单实体对象
     */
    @Override
    public PayOrderEntity createOrder(ShoppingCartEntity shopCartEntity) throws Exception {
        // 1. 查询当前用户是否存在掉单和未支付订单
        OrderAggregate unpaidorderAggregate = repository.queryUnPayOrder(shopCartEntity);
        OrderItemEntity unpaidOrderEntity = unpaidorderAggregate.getOrderItemEntity();
        if (null != unpaidOrderEntity && OrderStatusVO.PAY_WAITE.equals(unpaidOrderEntity.getOrderStatus())) {
            log.info("创建订单-存在，已存在未支付订单。userId:{} productId:{} orderId:{}", shopCartEntity.getUserId(), shopCartEntity.getProductId(), unpaidOrderEntity.getOrderItemId());
            return PayOrderEntity.builder()
                    .userId(shopCartEntity.getUserId())
                    .orderId(unpaidOrderEntity.getOrderItemId())
                    .payUrl(unpaidorderAggregate.getPayUrl())
                    .orderStatus(unpaidOrderEntity.getOrderStatus())
                    .build();
        } else if (null != unpaidOrderEntity && OrderStatusVO.CREATE.equals(unpaidOrderEntity.getOrderStatus())) {
            log.info("创建订单-存在，存在未创建支付单订单，创建支付单开始 userId:{} productId:{} orderId:{}", shopCartEntity.getUserId(), shopCartEntity.getProductId(), unpaidOrderEntity.getOrderItemId());
            PayOrderEntity payOrderEntity = this.doPrepayOrder(shopCartEntity.getUserId(), shopCartEntity.getProductId(), unpaidOrderEntity.getProductName(), unpaidOrderEntity.getOrderItemId(), unpaidOrderEntity.getTotalAmount());
            return PayOrderEntity.builder()
                    .userId(shopCartEntity.getUserId())
                    .orderId(unpaidOrderEntity.getOrderItemId())
                    .payUrl(unpaidorderAggregate.getPayUrl())
                    .orderStatus(unpaidOrderEntity.getOrderStatus())
                    .build();
        }

        // 2. 查询商品 & 聚合订单
        ProductEntity productEntity = productPort.queryProductByProductId(shopCartEntity.getProductId());

        OrderItemEntity orderItemEntity = OrderAggregate.createOrderItem(productEntity.getProductId(), productEntity.getProductName());
        orderItemEntity.setTotalAmount(productEntity.getPrice());

        OrderAggregate orderAggregate = OrderAggregate.builder()
                .userId(shopCartEntity.getUserId())
                .productEntity(productEntity)
                .orderItemEntity(orderItemEntity)
                .build();

        // 3. 保存订单 - 保存一份订单，再用订单生成ID生成支付单信息
        this.doSaveOrder(orderAggregate);

        // 4. 创建支付单
        PayOrderEntity payOrderEntity = this.doPrepayOrder(shopCartEntity.getUserId(), productEntity.getProductId(), productEntity.getProductName(), orderItemEntity.getOrderItemId(), productEntity.getPrice());
        log.info("创建订单-完成，生成支付单。userId: {} orderId: {} payUrl: {}", shopCartEntity.getUserId(), orderItemEntity.getOrderItemId(), payOrderEntity.getPayUrl());

        payOrderEntity.setUserId(shopCartEntity.getUserId());

        return payOrderEntity;
    }

    /**
     * 保存订单
     *
     * @param orderAggregate 订单聚合
     */
    protected abstract void doSaveOrder(OrderAggregate orderAggregate);

    /**
     * 预支付订单生成
     *
     * @param userId      用户ID
     * @param productId   商品ID
     * @param productName 商品名称
     * @param orderId     订单ID
     * @param totalAmount 支付金额
     * @return 预支付订单
     */
    protected abstract PayOrderEntity doPrepayOrder(String userId, String productId, String productName, String orderId, BigDecimal totalAmount) throws AlipayApiException;


}
