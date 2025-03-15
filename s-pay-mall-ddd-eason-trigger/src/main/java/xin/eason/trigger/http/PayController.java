package xin.eason.trigger.http;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import xin.eason.api.dto.CreatePayOrderDTO;
import xin.eason.api.dto.GroupBuyNotifyDTO;
import xin.eason.api.dto.PayOrderDTO;
import xin.eason.domain.order.model.entity.PayOrderEntity;
import xin.eason.domain.order.model.entity.ShoppingCartEntity;
import xin.eason.domain.order.model.valobj.MarketType;
import xin.eason.domain.order.service.IOrderService;
import xin.eason.types.common.Result;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/api/v1/alipay")
@RequiredArgsConstructor
public class PayController {

    @Value("${alipay.alipay-public-key}")
    private String alipayPublicKey;

    private final IOrderService orderService;

    /**
     * 创建流水订单和支付订单接口
     *
     * @param createPayOrderDTO 创建订单请求对象
     * @return 带有 用户ID, 订单ID, 订单状态 和 支付URL 的对象
     */
    @PostMapping("/create_pay_order")
    public Result<PayOrderDTO> pay(@RequestBody CreatePayOrderDTO createPayOrderDTO) {
        try {
            log.info("正在创建订单...");
            PayOrderEntity payOrderEntity = orderService.createOrder(
                    ShoppingCartEntity.builder()
                            .teamId(createPayOrderDTO.getTeamId())
                            .activityId(createPayOrderDTO.getActivityId())
                            .marketType(createPayOrderDTO.getMarketType() == 1 ? MarketType.MARKETED : MarketType.NO_MARKET)
                            .userId(createPayOrderDTO.getUserId())
                            .productId(createPayOrderDTO.getProductId())
                            .build()
            );
            log.info("创建订单成功!");
            PayOrderDTO payOrderDTO = new PayOrderDTO();
            payOrderDTO.setPayUrl(payOrderEntity.getPayUrl());
            payOrderDTO.setOrderStatus(payOrderEntity.getOrderStatus().getDesc());
            payOrderDTO.setOrderId(payOrderEntity.getOrderId());
            payOrderDTO.setUserId(payOrderEntity.getUserId());
            payOrderDTO.setDeductionAmount(payOrderEntity.getDeductionAmount());
            payOrderDTO.setPayAmount(payOrderEntity.getPayAmount());
            payOrderDTO.setMarketType(payOrderEntity.getMarketType().getCode());
            return Result.success(payOrderDTO);
        } catch (Exception e) {
            log.error("创建订单错误! 错误信息: ", e);
            return Result.error("创建订单错误");
        }
    }

    @PostMapping("/alipay_notify_url")
    public String alipayNotify(HttpServletRequest request) {
        try {
            log.info("支付回调，消息接收 {}", request.getParameter("trade_status"));

            if (request.getParameter("trade_status").equals("TRADE_SUCCESS")) {
                Map<String, String> params = new HashMap<>();
                Map<String, String[]> requestParams = request.getParameterMap();
                for (String name : requestParams.keySet()) {
                    params.put(name, request.getParameter(name));
                }

                String tradeNo = params.get("out_trade_no");
                String gmtPayment = params.get("gmt_payment");
                String alipayTradeNo = params.get("trade_no");

                String sign = params.get("sign");
                String content = AlipaySignature.getSignCheckContentV1(params);
                boolean checkSignature = AlipaySignature.rsa256CheckContent(content, sign, alipayPublicKey, "UTF-8"); // 验证签名
                // 支付宝验签
                if (checkSignature) {
                    // 验签通过
                    log.info("支付回调，交易名称: {}", params.get("subject"));
                    log.info("支付回调，交易状态: {}", params.get("trade_status"));
                    log.info("支付回调，支付宝交易凭证号: {}", params.get("trade_no"));
                    log.info("支付回调，商户订单号: {}", params.get("out_trade_no"));
                    log.info("支付回调，交易金额: {}", params.get("total_amount"));
                    log.info("支付回调，买家在支付宝唯一id: {}", params.get("buyer_id"));
                    log.info("支付回调，买家付款时间: {}", params.get("gmt_payment"));
                    log.info("支付回调，买家付款金额: {}", params.get("buyer_pay_amount"));
                    log.info("支付回调，支付回调，更新订单 {}", tradeNo);
                    // 更新数据库订单支付状态

                    orderService.orderPaySuccess(tradeNo, LocalDateTime.now());

                    return "success";
                }
            }
            return "false";
        } catch (RuntimeException | AlipayApiException e) {
            log.error("支付宝回调处理失败! 错误信息: \n", e);
            return "false";
        }
    }

    @PostMapping("/group_buy_notify")
    public String groupBuyNotify(@RequestBody GroupBuyNotifyDTO groupBuyNotifyDTO) {
        try {
            // 收到拼团系统回调请求
            String teamId = groupBuyNotifyDTO.getTeamId();
            List<String> orderIdList = groupBuyNotifyDTO.getOuterOrderId();
            // 批量将返回的 orderId 订单状态设置为 已完成, 并发货
            log.info("接收到拼团系统回调 requestParam: {}", groupBuyNotifyDTO);
            orderService.orderDelivery(orderIdList);
            return "success";
        } catch (Exception e) {
            log.error("设置 orderIdList: {} 订单状态为 已完成 过程出现异常! requestParam: {}", groupBuyNotifyDTO.getOuterOrderId(), groupBuyNotifyDTO, e);
            return "error";
        }
    }
}
