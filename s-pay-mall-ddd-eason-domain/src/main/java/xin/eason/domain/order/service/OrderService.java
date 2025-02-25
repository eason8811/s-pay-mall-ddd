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
import xin.eason.domain.order.model.entity.PayOrderEntity;
import xin.eason.domain.order.model.valobj.OrderStatusVO;

import java.math.BigDecimal;

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

    @Override
    protected PayOrderEntity doPrepayOrder(String userId, String productId, String productName, String orderId, BigDecimal totalAmount) throws AlipayApiException {
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

        log.info("创建支付订单成功, payOrder: {}", payOrderEntity);
        // 更新订单支付信息
        repository.updateOrderPayInfo(payOrderEntity);

        log.info("支付 URL: \n{}", payOrderEntity.getPayUrl());

        return payOrderEntity;
    }

    /**
     * 将指定订单编号的订单付款状态修改为支付成功
     *
     * @param tradeNo 订单编号
     */
    @Override
    public void changeOrderStatusSuccess(String tradeNo) {
        repository.changeOrderStatusSuccess(tradeNo);
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
}
