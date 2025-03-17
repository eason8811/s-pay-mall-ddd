package xin.eason.domain.order.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import xin.eason.domain.order.adapter.event.IPayEvent;
import xin.eason.domain.order.adapter.port.IPayPort;
import xin.eason.domain.order.adapter.port.IProductPort;
import xin.eason.domain.order.adapter.repository.IOrderRepository;
import xin.eason.domain.order.model.aggregate.OrderAggregate;
import xin.eason.domain.order.model.entity.*;
import xin.eason.domain.order.model.valobj.MarketType;
import xin.eason.domain.order.model.valobj.OrderStatusVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
     * @param shopCartEntity 购物车实体对象
     * @return 支付单实体对象
     */
    @Override
    @Transactional
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
                    .deductionAmount(unpaidOrderEntity.getDeductionAmount())
                    .payAmount(unpaidOrderEntity.getPayAmount())
                    .marketType(unpaidOrderEntity.getMarketType())
                    .build();
        } else if (null != unpaidOrderEntity && OrderStatusVO.CREATE.equals(unpaidOrderEntity.getOrderStatus())) {
            log.info("创建订单-存在，存在未创建支付单订单，创建支付单开始 userId:{} productId:{} orderId:{}", shopCartEntity.getUserId(), shopCartEntity.getProductId(), unpaidOrderEntity.getOrderItemId());
            // 获取未支付订单的拼团营销类型, 和折扣价格
            MarketType marketType = unpaidOrderEntity.getMarketType();
            BigDecimal deductionAmount = unpaidOrderEntity.getDeductionAmount();

            if (MarketType.NO_MARKET.equals(marketType)) {
                // 没有拼团营销
                return this.doPrepayOrder(shopCartEntity.getUserId(), shopCartEntity.getProductId(), unpaidOrderEntity.getProductName(), unpaidOrderEntity.getOrderItemId(), unpaidOrderEntity.getTotalAmount());
            }

            if (deductionAmount == null) {
                // 有拼团营销但是没有折扣价格, 即未进行拼团试算, 也就是说还没有进行锁单, 因此要进行锁单
                MarketPayDiscountEntity marketPayDiscountEntity = lockMarketOrder(shopCartEntity.getActivityId(), shopCartEntity.getTeamId(), shopCartEntity.getUserId(), shopCartEntity.getProductId(), unpaidOrderEntity.getOrderItemId());
                return this.doPrepayOrder(shopCartEntity.getUserId(), shopCartEntity.getProductId(), unpaidOrderEntity.getProductName(), unpaidOrderEntity.getOrderItemId(), unpaidOrderEntity.getTotalAmount(), marketPayDiscountEntity);
            }
            // 有拼团营销, 且有折扣价格, 直接进行 无拼团版预支付 但是价格不是原价 而是实际支付价格
            return this.doPrepayOrder(shopCartEntity.getUserId(), shopCartEntity.getProductId(), unpaidOrderEntity.getProductName(), unpaidOrderEntity.getOrderItemId(), unpaidOrderEntity.getPayAmount());
        }

        // 2. 查询商品 & 聚合订单
        ProductEntity productEntity = productPort.queryProductByProductId(shopCartEntity.getProductId());
        OrderItemEntity orderItemEntity = OrderAggregate.createOrderItem(productEntity.getProductId(), productEntity.getProductName(), shopCartEntity.getMarketType());
        orderItemEntity.setTotalAmount(productEntity.getPrice());

        OrderAggregate orderAggregate = OrderAggregate.builder()
                .userId(shopCartEntity.getUserId())
                .productEntity(productEntity)
                .orderItemEntity(orderItemEntity)
                .build();

        // 3. 保存本地订单 - 保存一份订单，再用订单生成ID生成支付单信息
        this.doSaveOrder(orderAggregate);

        // 4. 发起拼团营销锁单
        MarketPayDiscountEntity marketPayDiscountEntity = null;
        if (MarketType.MARKETED.equals(shopCartEntity.getMarketType())) {
            marketPayDiscountEntity = this.lockMarketOrder(shopCartEntity.getActivityId(), shopCartEntity.getTeamId(), shopCartEntity.getUserId(), shopCartEntity.getProductId(), orderItemEntity.getOrderItemId());
        }

        // 5. 创建支付单
        PayOrderEntity payOrderEntity = this.doPrepayOrder(shopCartEntity.getUserId(), productEntity.getProductId(), productEntity.getProductName(), orderItemEntity.getOrderItemId(), productEntity.getPrice(), marketPayDiscountEntity);
        log.info("创建订单-完成，生成支付单。userId: {} orderId: {} payUrl: {} discount: {}", shopCartEntity.getUserId(), orderItemEntity.getOrderItemId(), payOrderEntity.getPayUrl(), marketPayDiscountEntity);

        payOrderEntity.setUserId(shopCartEntity.getUserId());

        return payOrderEntity;
    }

    /**
     * 已经收到支付宝订单支付成功的回调后执行的操作
     *
     * @param tradeNo 订单 ID
     * @param payTime 支付时间
     */
    @Override
    public void orderPaySuccess(String tradeNo, LocalDateTime payTime) {
        OrderAggregate orderAggregate = repository.queryOrderById(tradeNo);
        OrderItemEntity orderItemEntity = orderAggregate.getOrderItemEntity();
        if (MarketType.NO_MARKET.equals(orderItemEntity.getMarketType())) {
            // 支付成功后, 如果支付成功的订单营销类型为 无营销 则直接修改本系统的本订单支付状态
            changeOrderStatusSuccess(List.of(tradeNo), payTime);
            sendPaySuccessEvent(tradeNo);
            return;
        }
        // 如果订单营销类型为 有营销 则请求拼团系统进行结算, 保证拼团与本系统的数据一致性, 并将本系统订单状态修改为已支付
        String orderId = payPort.settlementMarketOrder(orderAggregate.getUserId(), tradeNo, payTime);
        changeOrderStatusSuccess(List.of(orderId), payTime);
        // 待拼团系统回调本系统后, 再修改本系统的订单状态为已完成, 然后进行发货
        // 同时创建定期扫描的兜底
    }

    /**
     * 锁定拼团订单
     * @param activityId 活动 ID
     * @param teamId 拼团队伍 ID
     * @param userId 用户 ID
     * @param productId 商品 ID
     * @param orderItemId 订单明细 ID
     * @return 营销支付折扣信息实体, 包括商品原价, 折扣价格, 支付价格
     */
    protected abstract MarketPayDiscountEntity lockMarketOrder(Long activityId, String teamId, String userId, String productId, String orderItemId);

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

    /**
     * 预支付订单生成
     *
     * @param userId      用户ID
     * @param productId   商品ID
     * @param productName 商品名称
     * @param orderId     订单ID
     * @param totalAmount 支付金额
     * @param marketPayDiscountEntity 营销订单支付折扣信息实体
     * @return 预支付订单
     */
    protected abstract PayOrderEntity doPrepayOrder(String userId, String productId, String productName, String orderId, BigDecimal totalAmount, MarketPayDiscountEntity marketPayDiscountEntity) throws AlipayApiException;


}
