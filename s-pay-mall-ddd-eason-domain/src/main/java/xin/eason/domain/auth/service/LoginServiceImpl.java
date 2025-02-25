package xin.eason.domain.auth.service;

import com.google.common.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xin.eason.domain.auth.adapter.port.ILoginPort;
import xin.eason.types.exception.AppException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements ILoginService {

    private final Cache<String, String> loginCache;

    private final ILoginPort loginPort;

    /**
     * 储存用户的登录状态以及登录信息
     *
     * @param ticket 生成二维码的 Ticket
     * @param openid 用户的 OpenId
     */
    @Override
    public void saveLoginState(String ticket, String openid) {
        // 使用 Cache 储存用户信息
        if (loginCache.getIfPresent(ticket) == null)
            loginCache.put(ticket, openid);

        //发送登陆成功模板信息
        loginPort.sendLoginTemplate(openid);
    }

    /**
     * 获取 <b>Ticket</b>
     *
     * @return 返回获取到的 <b>Ticket</b>
     */
    @Override
    public String getTicket() {
        try {
            return loginPort.getTicket();
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
    }

    /**
     * 检查登录状态
     *
     * @param ticket 用于生成二维码的 <b>Ticket</b>
     * @return 用户信息的 Map
     */
    @Override
    public Map<String, String> checkLogin(String ticket) {
        log.info("正在查询登录状态...");
        String openId = loginCache.getIfPresent(ticket);
        String loginStatus = "未登录";
        Map<String, String> dataMap = new HashMap<>();

        if (openId != null && !openId.isBlank()) {
            loginStatus = "已登录";
        }
        log.info("用户: {}, {}!", openId, loginStatus);
        dataMap.put("userOpenId", openId);
        dataMap.put("ticket", ticket);
        dataMap.put("loginStatus", loginStatus);
        return dataMap;
    }
}
