package xin.eason.infrastructure.adapter.port;

import com.google.common.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import retrofit2.Response;
import xin.eason.domain.auth.adapter.port.ILoginPort;
import xin.eason.infrastructure.gateway.IWechatService;
import xin.eason.infrastructure.gateway.dto.*;
import xin.eason.types.common.Constants;
import xin.eason.types.exception.AppException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginPort implements ILoginPort {

    private final String LOGIN_TEMP_ID = "EZXZgdMPpQUabzSAU5tj8gc05zoQ9e-BToUdU02WhiY";

    private final String LOCATION_TEMP_ID = "pKIT3l2hfdpyUK4x1N8ugHddvty-N4l5VifX0j3sEFA";

    private final String SEND_SUCCESS_TEMP_ID = "BNGoy95cPSy1mjsFleykVN811OtXBO2rrsMUPuFMpKY";

    @Value("${wechat.grant-type}")
    private String grant_type;

    @Value("${wechat.appid}")
    private String appid;

    @Value("${wechat.secret}")
    private String secret;

    private final Cache<String, String> cache;

    private final IWechatService iWechatService;

    /**
     * 发送登陆成功的模板信息
     *
     * @param openid 用户的 OpenId
     */
    @Override
    public void sendLoginTemplate(String openid) {
        HashMap<String, String> dataMap = new HashMap<>();
        dataMap.put("openId", openid);
        sendTemplate(LOCATION_TEMP_ID, openid, "", cache.getIfPresent("access_token"), dataMap);
    }

    /**
     * 发送用户位置信息
     *
     * @param openid 用户的 OpenId
     */
    @Override
    public void sendLocationTemplate(String openid) {

    }

    /**
     * 发送用户购买的商品发货成功的信息
     *
     * @param openid 用户的 OpenId
     */
    @Override
    public void sendSendSuccessTemplate(String openid) {

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

    @Override
    public String getTicket() {
        // 判断是否存有access_token, 若没有, 则获取
        String accessToken = cache.getIfPresent("access_token");
        try {
            log.info("正在获取 Access Token");
            if (accessToken == null || accessToken.isBlank()) {
                log.info("缓存无 Access Token 向微信公众平台获取");
                Response<WechatResponseDTO> response = iWechatService.getAccessToken(grant_type, appid, secret).execute();
                accessToken = response.body().getAccessToken();
                assert accessToken != null && !accessToken.isBlank();
            }
        } catch (IOException e) {
            log.error("获取 Access Token 失败");
            throw new AppException(e.getMessage());
        }
        cache.put("access_token", accessToken);

        log.info("获取 Access Token 成功: {}", accessToken);
        log.info("正在获取 Ticket");
        WechatQrCodeRequestDTO wechatQrCodeReq = WechatQrCodeRequestDTO.builder()
                .actionName(Constants.WECHAT_QR_CODE_ACTION_NAME)
                .actionInfo(
                        WechatQrCodeRequestDTO.ActionInfo.builder()
                                .scene(WechatQrCodeRequestDTO.Scene.builder().scene_str(String.valueOf(System.currentTimeMillis())).build())
                                .build()
                )
                .build();

        try {
            Response<WechatQrCodeResponseDTO> response = iWechatService.getQrCode(wechatQrCodeReq, accessToken).execute();
            WechatQrCodeResponseDTO qrCodeRes = response.body();
            String ticket = qrCodeRes.getTicket();

            assert ticket != null && !ticket.isBlank();

            log.info("获取 Ticket 成功: {}", ticket);

            return ticket;
        } catch (IOException e) {
            log.error("获取 Ticket 失败");
            throw new AppException(e.getMessage());
        }
    }
}

