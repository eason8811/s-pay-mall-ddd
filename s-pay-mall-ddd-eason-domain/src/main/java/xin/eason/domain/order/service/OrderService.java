package xin.eason.domain.order.service;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xin.eason.domain.order.adapter.event.IPayEvent;
import xin.eason.domain.order.adapter.port.IPayPort;
import xin.eason.domain.order.adapter.port.IProductPort;
import xin.eason.domain.order.adapter.repository.IOrderRepository;
import xin.eason.domain.order.model.aggregate.OrderAggregate;
import xin.eason.domain.order.model.entity.MarketPayDiscountEntity;
import xin.eason.domain.order.model.entity.PayOrderEntity;
import xin.eason.domain.order.model.valobj.MarketType;
import xin.eason.domain.order.model.valobj.OrderStatusVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class OrderService extends AbstractOrderService {

    @Value("${alipay.notify-url}")
    private String notifyUrl;
    @Value("${alipay.return-url}")
    private String returnUrl;

    public OrderService(IOrderRepository repository, IProductPort productPort, IPayEvent payEvent, AlipayClient alipayClient, IPayPort payPort) {
        super(repository, productPort, payEvent, alipayClient, payPort);
    }

    @Override
    protected void doSaveOrder(OrderAggregate orderAggregate) {
        repository.doSaveOrder(orderAggregate);
    }

    /**
     * 预支付订单生成 (无拼团策略版)
     * @param userId      用户ID
     * @param productId   商品ID
     * @param productName 商品名称
     * @param orderId     订单ID
     * @param totalAmount 支付金额
     * @return 预支付订单
     * @throws AlipayApiException 支付宝 API 异常
     */
    @Override
    protected PayOrderEntity doPrepayOrder(String userId, String productId, String productName, String orderId, BigDecimal totalAmount) throws AlipayApiException {
        return doPrepayOrder(userId, productId, productName, orderId, totalAmount, null);
    }

    /**
     * 锁定拼团订单
     *
     * @param activityId  活动 ID
     * @param teamId      拼团队伍 ID
     * @param userId      用户 ID
     * @param productId   商品 ID
     * @param orderItemId 订单明细 ID
     * @return 营销支付折扣信息实体, 包括商品原价, 折扣价格, 支付价格
     */
    @Override
    protected MarketPayDiscountEntity lockMarketOrder(Long activityId, String teamId, String userId, String productId, String orderItemId) {
        return payPort.lockMarketOrder(activityId, teamId, userId, productId, orderItemId);
    }

    /**
     * 预支付订单生成 (有拼团策略版)
     *
     * @param userId                  用户ID
     * @param productId               商品ID
     * @param productName             商品名称
     * @param orderId                 订单ID
     * @param totalAmount             支付金额
     * @param marketPayDiscountEntity 营销订单支付折扣信息实体
     * @return 预支付订单
     */
    @Override
    protected PayOrderEntity doPrepayOrder(String userId, String productId, String productName, String orderId, BigDecimal totalAmount, MarketPayDiscountEntity marketPayDiscountEntity) throws AlipayApiException {
        BigDecimal payAmount = totalAmount;
        if (marketPayDiscountEntity != null)
            payAmount = marketPayDiscountEntity.getPayAmount();

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderId);
        bizContent.put("total_amount", totalAmount.toString());
        bizContent.put("subject", productName);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());

        String form = alipayClient.pageExecute(request).getBody();

        PayOrderEntity payOrderEntity = new PayOrderEntity();
        payOrderEntity.setOrderId(orderId);
        payOrderEntity.setPayUrl(form);
        payOrderEntity.setOrderStatus(OrderStatusVO.PAY_WAITE);

        // 拼团信息
        payOrderEntity.setPayAmount(payAmount);
        payOrderEntity.setDeductionAmount(marketPayDiscountEntity != null ? marketPayDiscountEntity.getDeductionAmount() : BigDecimal.ZERO);
        payOrderEntity.setMarketType(marketPayDiscountEntity != null ? MarketType.MARKETED : MarketType.NO_MARKET);

        log.info("创建支付订单成功, payOrder: {}", payOrderEntity);
        // 更新订单支付信息
        repository.updateOrderPayInfo(payOrderEntity);

        log.info("支付 URL: \n{}", payOrderEntity.getPayUrl());

        return payOrderEntity;
    }

    /**
     * 将指定订单编号的订单付款状态修改为支付成功
     * @param orderIdList 订单编号列表
     * @param payTime 支付时间
     */
    @Override
    public void changeOrderStatusSuccess(List<String> orderIdList, LocalDateTime payTime) {
        repository.changeOrderStatusSuccess(orderIdList, payTime);
    }

    /**
     * 发送指定订单编号的订单支付成功的消息
     *
     * @param tradeNo 订单编号
     */
    @Override
    public void sendPaySuccessEvent(String tradeNo) {
        payEvent.sendPaySuccessEvent(tradeNo);
    }

    /**
     * 发送 发货成功 的模板信息
     *
     * @param orderId 订单 ID
     */
    @Override
    public void sendDeliverySuccessMsg(String orderId) {
        OrderAggregate orderAggregate = repository.queryOrderById(orderId);
        payPort.sendSendSuccessTemplate(orderAggregate);
    }

    /**
     * 根据订单 ID 将订单状态修改为完成
     *
     * @param orderId 订单 ID
     */
    @Override
    public void doneOrderById(String orderId) {
        PayOrderEntity payOrderEntity = PayOrderEntity.builder()
                .orderStatus(OrderStatusVO.DEAL_DONE)
                .orderId(orderId)
                .build();
        repository.updateOrderPayInfo(payOrderEntity);
    }

    /**
     * 将 orderIdList 中的订单进行发货
     *
     * @param orderIdList 订单 ID 列表
     */
    @Override
    public void orderDelivery(List<String> orderIdList) {
        orderIdList.forEach(orderId -> payEvent.sendPaySuccessEvent(orderId));
    }
}
