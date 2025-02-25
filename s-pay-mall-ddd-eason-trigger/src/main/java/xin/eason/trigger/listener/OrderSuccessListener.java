package xin.eason.trigger.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import xin.eason.domain.order.service.IOrderService;


@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSuccessListener {

    private final IOrderService orderService;

    private static final String PAY_SUCCESS_ROUTING_KEY = "pay.success";
    private static final String PAY_SUCCESS_EXCHANGE = "amq.direct";
    private static final String PAY_SUCCESS_QUEUE = "pay.success.queue";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(PAY_SUCCESS_QUEUE),
            key = PAY_SUCCESS_ROUTING_KEY,
            exchange = @Exchange(PAY_SUCCESS_EXCHANGE)
    ))
    public void listener(String paySuccessOrderId) {
        try {
            log.info("已收到支付成功回调 MQ 消息: {}", paySuccessOrderId);
            // 根据 orderId 修改订单状态为 DEAL_DONE 交易完成
            orderService.doneOrderById(paySuccessOrderId);
            // 发送订单发货成功的模板信息
            orderService.sendDeliverySuccessMsg(paySuccessOrderId);

        } catch (Exception e) {
            log.error("更新支付成功订单信息出错! 订单ID: {}", paySuccessOrderId);
        }
    }
}
