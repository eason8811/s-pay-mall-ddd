package xin.eason.infrastructure.adapter.port;

import com.google.common.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import retrofit2.Response;
import xin.eason.domain.order.adapter.port.IPayPort;
import xin.eason.domain.order.model.aggregate.OrderAggregate;
import xin.eason.domain.order.model.entity.MarketPayDiscountEntity;
import xin.eason.infrastructure.gateway.IGroupBuyMarketService;
import xin.eason.infrastructure.gateway.IWechatService;
import xin.eason.infrastructure.gateway.dto.*;
import xin.eason.types.common.Result;
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

    @Value("${app.config.group-buy-market.source}")
    private String source;
    @Value("${app.config.group-buy-market.chanel}")
    private String chanel;
    @Value("${app.config.group-buy-market.notify-url}")
    private String notifyUrl;

    private final String SEND_SUCCESS_TEMP_ID = "BNGoy95cPSy1mjsFleykVN811OtXBO2rrsMUPuFMpKY";

    private final IWechatService iWechatService;

    private final IGroupBuyMarketService iGroupBuyMarketService;

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

    /**
     * 进行拼团订单锁定
     *
     * @param activityId  活动 ID
     * @param teamId      拼团队伍 ID
     * @param userId      用户 ID
     * @param productId   商品 ID
     * @param orderItemId 本地订单明细 ID
     * @return 拼团订单支付折扣实体
     */
    @Override
    public MarketPayDiscountEntity lockMarketOrder(Long activityId, String teamId, String userId, String productId, String orderItemId) {
        try {
            LockMarketPayOrderRequestDTO requestDTO = LockMarketPayOrderRequestDTO.builder()
                    .userId(userId)
                    .teamId(teamId)
                    .activityId(activityId)
                    .goodsId(productId)
                    .source(source)
                    .channel(chanel)
                    .outerOrderId(orderItemId)
                    .notifyUrl(notifyUrl)
                    .build();

            Result<LockMarketPayOrderResponseDTO> response = iGroupBuyMarketService.lockMarketPayOrder(requestDTO).execute().body();
            log.info("拼团订单锁定, userId: {}, request: {}, response: {}", userId, requestDTO, response);
            if (response == null)
                return null;
            if (response.getCode() != 1)
                throw new AppException("拼团订单锁定异常! 锁单服务响应代码为: " + response.getCode());

            LockMarketPayOrderResponseDTO data = response.getData();

            return MarketPayDiscountEntity.builder()
                    .originalAmount(data.getOriginalPrice())
                    .deductionAmount(data.getDiscountPrice())
                    .payAmount(data.getPayPrice())
                    .build();
        } catch (IOException e) {
            log.error("营销锁单失败 userId: {}, orderItemId: {}", userId, orderItemId, e);
            return null;
        }
    }

    /**
     * 进行拼团订单结算
     *
     * @param userId  用户 ID
     * @param tradeNo 本地订单明细 ID
     * @param payTime 支付时间
     * @return 支付成功的订单 ID
     */
    @Override
    public String settlementMarketOrder(String userId, String tradeNo, LocalDateTime payTime) {
        try {
            SettlementOrderRequestDTO requestDTO = SettlementOrderRequestDTO.builder()
                    .source(source)
                    .channel(chanel)
                    .userId(userId)
                    .outerOrderId(tradeNo)
                    .payTime(payTime)
                    .build();
            Result<SettlementOrderResponseDTO> response = iGroupBuyMarketService.settlementMarketPayOrder(requestDTO).execute().body();
            log.info("拼团订单结算, userId: {}, request: {}, response: {}", userId, requestDTO, response);
            if (response == null)
                return null;
            if (response.getCode() != 1)
                throw new AppException("拼团订单锁定异常! 锁单服务响应代码为: " + response.getCode());

            // 返回支付成功的本系统订单 ID
            return response.getData().getOuterOrderId();

        } catch (IOException e) {
            log.error("营销结算失败 userId: {}, orderItemId: {}", userId, tradeNo, e);
            return null;
        }
    }
}
