package xin.eason.infrastructure.adapter.port;

import com.google.common.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import retrofit2.Response;
import xin.eason.domain.order.adapter.port.IPayPort;
import xin.eason.domain.order.model.aggregate.OrderAggregate;
import xin.eason.infrastructure.gateway.IWechatService;
import xin.eason.infrastructure.gateway.dto.WechatTemplateRequestDTO;
import xin.eason.infrastructure.gateway.dto.WechatTemplateResponseDTO;
import xin.eason.types.exception.AppException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayPort implements IPayPort {

    private final String SEND_SUCCESS_TEMP_ID = "BNGoy95cPSy1mjsFleykVN811OtXBO2rrsMUPuFMpKY";

    private final IWechatService iWechatService;

    private final Cache<String, String> cache;

    /**
     * 发送用户购买的商品发货成功的信息
     * @param orderAggregate 订单聚合
     */
    @Override
    public void sendSendSuccessTemplate(OrderAggregate orderAggregate) {
        // 装配数据
        HashMap<String, String> dataMap = new HashMap<>();
        dataMap.put("orderId", orderAggregate.getOrderItemEntity().getOrderItemId());
        dataMap.put("productName", orderAggregate.getOrderItemEntity().getProductName());
        dataMap.put("time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        sendTemplate(SEND_SUCCESS_TEMP_ID, orderAggregate.getUserId(), "", cache.getIfPresent("access_token"), dataMap);
    }

    /**
     * 向指定用户发送模板信息
     *
     * @param templateId  需要发送的模板信息的 <b>模板 ID</b>
     * @param openid      需要发送的用户的 <b>openId</b>
     * @param turnToUrl   发送给用户的模板信息点击后跳转的 <b>URL</b>
     * @param accessToken 用于向微信公众平台调用 API 的 <b>Access Token</b>
     * @param dataMap     用于存储数据的键值对
     * @return 如果发送成功则返回模板信息的 <b>ID</b>
     */
    @Override
    public Long sendTemplate(String templateId, String openid, String turnToUrl, String accessToken, Map<String, String> dataMap) {
        // 发送模板信息
        WechatTemplateRequestDTO request = new WechatTemplateRequestDTO();
        request.setTemplateId(templateId);
        request.setTouser(openid);
        request.setUrl(turnToUrl);
        dataMap.forEach(request::put);
        // 向公众平台发送 http 请求
        try {
            Response<WechatTemplateResponseDTO> response = iWechatService.sendTemplate(request, accessToken).execute();
            String errmsg = response.body().getErrmsg();
            if (!"ok".equals(errmsg)) {
                log.error("发送模板信息失败! 错误代码: {}, 错误信息: {}", response.body().getErrcode(), errmsg);
                throw new AppException(errmsg);
            }
            Long msgid = response.body().getMsgid();
            log.info("发送模板信息成功, msgId: {}", msgid);
            return msgid;
        } catch (IOException e) {
            log.error("发送模板信息请求过程出错!");
            throw new AppException(e.getMessage());
        }
    }
}
