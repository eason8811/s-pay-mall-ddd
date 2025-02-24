package xin.eason.domain.auth.service;


import java.util.Map;

public interface ILoginService {

    /**
     * 储存用户的登录状态以及登录信息
     * @param ticket 生成二维码的 Ticket
     * @param openid 用户的 OpenId
     */
    void saveLoginState(String ticket, String openid);

    /**
     * 获取 <b>Ticket</b> 业务流程
     *
     * @return 返回获取到的 <b>Ticket</b>
     */
    String getTicket();

    /**
     * 检查登录状态
     * @param ticket 用于生成二维码的 <b>Ticket</b>
     * @return 用户信息的 Map
     */
    Map<String, String> checkLogin(String ticket);
}
