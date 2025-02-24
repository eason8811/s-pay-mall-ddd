package xin.eason.domain.auth.adapter.port;

import java.util.Map;

public interface ILoginPort {

    /**
     * 发送登陆成功的模板信息
     * @param openid 用户的 OpenId
     */
    void sendLoginTemplate(String openid);

    /**
     * 发送用户位置信息
     * @param openid 用户的 OpenId
     */
    void sendLocationTemplate(String openid);

    /**
     * 发送用户购买的商品发货成功的信息
     * @param openid 用户的 OpenId
     */
    void sendSendSuccessTemplate(String openid);

    /**
     * 向指定用户发送模板信息
     *
     * @param templateId  需要发送的模板信息的 <b>模板 ID</b>
     * @param openid      需要发送的用户的 <b>openId</b>
     * @param turnToUrl   发送给用户的模板信息点击后跳转的 <b>URL</b>
     * @param accessToken 用于向微信公众平台调用 API 的 <b>Access Token</b>
     * @param dataMap 用于存储数据的键值对
     * @return 如果发送成功则返回模板信息的 <b>ID</b>
     */
    Long sendTemplate(String templateId, String openid, String turnToUrl, String accessToken, Map<String, String> dataMap);

    /**
     * 获取 Ticket
     * @return Ticket 字符串
     */
    String getTicket();

}
