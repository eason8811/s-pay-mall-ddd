package xin.eason.infrastructure.adapter.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.BiConsumer;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import xin.eason.domain.order.adapter.event.IPayEvent;

@Slf4j
public abstract class AbstractPayEvent implements IPayEvent {
    public CorrelationData initCorrelationData() {
        CorrelationData cd = new CorrelationData();
        cd.getFuture().whenComplete(new BiConsumer<CorrelationData.Confirm, Throwable>() {
            @Override
            public void accept(CorrelationData.Confirm confirm, Throwable throwable) {
                if (throwable != null) {
                    log.error("发送过程出现错误! 错误信息: ", throwable);
                    return;
                }
                if (confirm.isAck()) {
                    log.info("发送成功! 返回: {}", confirm);
                } else {
                    log.info("发送失败! 返回: {}", confirm.getReason());
                }
            }
        });
        return cd;
    }
}
