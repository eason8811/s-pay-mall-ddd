package xin.eason.trigger.http;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import xin.eason.api.dto.CreatePayOrderDTO;
import xin.eason.api.dto.PayOrderDTO;
import xin.eason.domain.order.model.entity.PayOrderEntity;
import xin.eason.domain.order.model.entity.ShoppingCartEntity;
import xin.eason.domain.order.service.IOrderService;
import xin.eason.types.common.Result;

import java.util.HashMap;
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
     * @param shopCartReq 购物车请求对象, 包含了 <b>用户ID</b> 和 <b>商品ID</b>
     * @return 带有 用户ID, 订单ID, 订单状态 和 支付URL 的对象
     */
    @PostMapping("/create_pay_order")
    public Result<PayOrderDTO> pay(@RequestBody CreatePayOrderDTO createPayOrderDTO) {
        try {
            log.info("正在创建订单...");
            PayOrderEntity payOrderEntity = orderService.createOrder(
                    ShoppingCartEntity.builder()
                            .productId(createPayOrderDTO.getProductId())
                            .productId(createPayOrderDTO.getProductId())
                            .userId(createPayOrderDTO.getUserId())
                            .build()
            );
            log.info("创建订单成功!");
            PayOrderDTO payOrderDTO = new PayOrderDTO();
            payOrderDTO.setPayUrl(payOrderEntity.getPayUrl());
            payOrderDTO.setOrderStatus(payOrderEntity.getOrderStatus().getDesc());
            payOrderDTO.setOrderId(payOrderEntity.getOrderId());
            payOrderDTO.setUserId(payOrderEntity.getUserId());
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

                    orderService.changeOrderStatusSuccess(tradeNo);
                    // 向支付成功消息队列发送消息
                    orderService.sendPaySuccessEvent(tradeNo);

                    return "success";
                }
            }
            return "false";
        } catch (RuntimeException | AlipayApiException e) {
            log.error("支付宝回调处理失败! 错误信息: \n", e);
            return "false";
        }
    }


}
