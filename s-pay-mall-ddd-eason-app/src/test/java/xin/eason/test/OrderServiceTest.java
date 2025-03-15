package xin.eason.test;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import xin.eason.domain.order.model.entity.PayOrderEntity;
import xin.eason.domain.order.model.entity.ShoppingCartEntity;
import xin.eason.domain.order.model.valobj.MarketType;
import xin.eason.domain.order.service.IOrderService;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderServiceTest {

    @Autowired
    private IOrderService orderService;

    @Test
    public void test_createOrder() throws Exception {
        ShoppingCartEntity shopCartEntity = new ShoppingCartEntity();
        shopCartEntity.setUserId("Eason4"); // 每次测试用个新的id就可以，不限制人群的情况下，可以随意编写。
        shopCartEntity.setProductId("9890001");
        shopCartEntity.setTeamId(null);
        shopCartEntity.setActivityId(100123L);
        shopCartEntity.setMarketType(MarketType.MARKETED);

        PayOrderEntity payOrderEntity = orderService.createOrder(shopCartEntity);

        log.info("请求参数:{}", JSON.toJSONString(shopCartEntity));
        log.info("测试结果:{}", JSON.toJSONString(payOrderEntity));
    }

}