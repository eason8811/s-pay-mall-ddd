package xin.eason.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RabbitConf {

    private final RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init(){
        rabbitTemplate.setReturnsCallback(returnedMessage -> {
            log.info("触发 Rabbit MQ 的 Callback 函数");
            log.info("交换机为: {}", returnedMessage.getExchange());
            log.info("信息为: {}", returnedMessage.getMessage());
            log.info("回复内容为: {}", returnedMessage.getReplyText());
            log.info("回复代码为: {}", returnedMessage.getReplyCode());
            log.info("路由键为: {}", returnedMessage.getRoutingKey());
        });
    }


}
