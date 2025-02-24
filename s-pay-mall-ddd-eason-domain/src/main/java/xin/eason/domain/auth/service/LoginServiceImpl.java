package xin.eason.domain.auth.service;

import com.google.common.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xin.eason.domain.auth.adapter.port.ILoginPort;
import xin.eason.domain.auth.model.valobj.UserInfoVO;
import xin.eason.types.exception.AppException;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements ILoginService {

    private final Cache<String, String> loginCache;

    private final ILoginPort loginPort;

    /**
     * 储存用户的登录状态以及登录信息
     * @param ticket 生成二维码的 Ticket
     * @param openid 用户的 OpenId
     */
    @Override
    public void saveLoginState(String ticket, String openid) {
        // 使用 Cache 储存用户信息
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
     * @param ticket 用于生成二维码的 <b>Ticket</b>
     * @return 用户信息的视图对象
     */
    @Override
    public UserInfoVO checkLogin(String ticket) {
        return null;
    }
}
