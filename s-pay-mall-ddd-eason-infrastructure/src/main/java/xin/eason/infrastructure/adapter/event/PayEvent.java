package xin.eason.infrastructure.adapter.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayEvent extends AbstractPayEvent {

    private final RabbitTemplate rabbitTemplate;

    private static final String PAY_SUCCESS_ROUTING_KEY = "pay.success";
    private static final String PAY_SUCCESS_EXCHANGE = "amq.direct";
    private static final String PAY_SUCCESS_QUEUE = "pay.success.queue";

    private CorrelationData correlationData;

    /**
     * 发送指定订单编号的订单支付成功的消息
     * @param tradeNo 订单编号
     */
    @Override
    public void sendPaySuccessEvent(String tradeNo) {
        if (correlationData == null) {
            correlationData = initCorrelationData();
        }
        rabbitTemplate.convertAndSend(PAY_SUCCESS_EXCHANGE, PAY_SUCCESS_ROUTING_KEY, tradeNo, correlationData);
    }


}
