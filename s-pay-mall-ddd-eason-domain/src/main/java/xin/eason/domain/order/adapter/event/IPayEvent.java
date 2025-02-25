package xin.eason.domain.order.adapter.event;

public interface IPayEvent {

    /**
     * 发送指定订单编号的订单支付成功的消息
     * @param tradeNo 订单编号
     */
    void sendPaySuccessEvent(String tradeNo);
}
